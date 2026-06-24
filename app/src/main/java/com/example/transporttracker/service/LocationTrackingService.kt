package com.example.transporttracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.transporttracker.MainActivity
import com.example.transporttracker.widget.TripWidgetProvider
import com.example.transporttracker.R
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.data.local.entity.TripSegmentEntity
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.domain.model.McdEntrance
import com.example.transporttracker.domain.model.MetroEntrance
import com.example.transporttracker.domain.model.Stop
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.McdMatcher
import com.example.transporttracker.domain.usecase.MetroMatcher
import com.example.transporttracker.domain.usecase.SegmentVoter
import com.example.transporttracker.domain.usecase.StopMatcher
import com.example.transporttracker.domain.usecase.TripAnalyzer
import com.example.transporttracker.domain.usecase.TripDetector
import com.example.transporttracker.domain.usecase.TripEvent
import com.example.transporttracker.domain.usecase.WalkDetector
import com.example.transporttracker.ui.components.nameResId
import com.example.transporttracker.ui.home.TrackingState
import com.example.transporttracker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.transporttracker.utils.LocationUtils
import com.example.transporttracker.utils.McdParser
import com.example.transporttracker.utils.MetroParser
import com.example.transporttracker.utils.StopsParser
import com.google.android.gms.location.*
import kotlinx.coroutines.*

@AndroidEntryPoint
class LocationTrackingService : Service() {

    // --- signal history for stable transport detection ---
    private val metroHistory = ArrayDeque<Boolean>()
    private val mcdHistory = ArrayDeque<Boolean>()

    // Latched per-segment: once metro entrance proximity fires, keep voting METRO
    // until the segment is sealed (prevents tunnel GPS noise from washing out the signal)
    private var segmentMetroDetected = false

    // --- detectors ---
    private val walkDetector = WalkDetector()
    private val mcdMatcher = McdMatcher()
    private val metroMatcher = MetroMatcher()
    private val stopMatcher = StopMatcher()
    private val analyzer = TripAnalyzer()
    private val tripDetector = TripDetector()

    // --- static data loaded from assets ---
    private lateinit var stops: List<Stop>
    private lateinit var metroEntrances: List<MetroEntrance>
    private lateinit var mcdEntrances: List<McdEntrance>

    // --- infrastructure ---
    // limitedParallelism(1) gives a single-threaded serial dispatcher: all shared
    // mutable state (histories, sample lists, trip counters) is accessed from one
    // thread, eliminating data races without any locking.
    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default.limitedParallelism(1)
    )

    @Inject
    lateinit var repository: TransportRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // --- trip state ---
    private var currentTripId: Long? = null
    private var tripStartTime = 0L
    private var lastLocation: Location? = null
    private var gpsLostStart = 0L
    private var gpsLostDuration = 0L
    private var totalDistanceMeters = 0f
    private var lastNotifiedTransport = TransportType.UNKNOWN

    // --- per-segment accumulators ---
    private val speedSamples = mutableListOf<Float>()
    private val transportSamples = mutableListOf<TransportType>()

    // --- segment tracking ---
    private var segmentStartTime = 0L
    private var transferPauseStart = 0L
    private val completedSegments = mutableListOf<FinishedSegment>()

    private data class FinishedSegment(
        val startTime: Long,
        val endTime: Long,
        val transportType: TransportType,
        val averageSpeed: Float,
        val savedToDb: Boolean = false
    )

    private data class BufferedPoint(
        val timestamp: Long,
        val latitude: Double,
        val longitude: Double,
        val speed: Float,
        val accuracy: Float
    )

    private val preTripBuffer = ArrayDeque<BufferedPoint>()
    private val PRE_TRIP_BUFFER_MS = 120_000L

    // -------------------------------------------------------------------------

    override fun onCreate() {
        super.onCreate()

        TrackingState.isTracking = true

        stops = StopsParser.parse(this)
        metroEntrances = MetroParser.parse(this)
        mcdEntrances = McdParser.parse(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        startForeground(Constants.NOTIFICATION_ID, buildNotification())

        // Restore any trip that was interrupted when the service was killed
        serviceScope.launch {
            val activeTrip = repository.getActiveTrip()
            if (activeTrip != null) {
                val tripAge = System.currentTimeMillis() - activeTrip.startTime
                if (tripAge > Constants.MAX_TRIP_DURATION_MS) {
                    // Trip is too old to be real (e.g. service died overnight) — discard it
                    repository.deleteAbandonedTrip(activeTrip.id)
                    Log.d("TRIP_DEBUG", "Abandoned stale trip id=${activeTrip.id} age=${tripAge / 60000}min")
                } else {
                    tripStartTime = activeTrip.startTime
                    segmentStartTime = System.currentTimeMillis()
                    currentTripId = activeTrip.id
                    tripDetector.forceActive()

                    // Load already-saved segments so finishTrip can pick correct primary transport
                    val saved = repository.getSegmentsForTrip(activeTrip.id)
                    saved.forEach { seg ->
                        completedSegments.add(
                            FinishedSegment(
                                startTime = seg.startTime,
                                endTime = seg.endTime,
                                transportType = runCatching {
                                    TransportType.valueOf(seg.transportType)
                                }.getOrDefault(TransportType.UNKNOWN),
                                averageSpeed = seg.averageSpeed,
                                savedToDb = true
                            )
                        )
                    }
                    Log.d("TRIP_DEBUG", "Restored trip id=$currentTripId segments=${saved.size}")
                }
            }
        }

        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startLocationUpdates() {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_INTERVAL
        )
            .setMinUpdateIntervalMillis(Constants.LOCATION_FASTEST_INTERVAL)
            .build()

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                result.locations.forEach { location ->

                    val timestamp = location.time

                    val speed = if (lastLocation != null) {
                        LocationUtils.calculateSpeedMps(lastLocation!!, location)
                    } else {
                        0f
                    }

                    val distance = if (lastLocation != null && speed <= 28f) {
                        lastLocation!!.distanceTo(location)
                    } else 0f

                    lastLocation = location

                    // Filter GPS spikes (> 28 m/s ≈ 100 km/h — no urban transit goes faster)
                    if (speed > 28f) {
                        Log.d("GPS_FILTER", "Ignored spike speed=$speed")
                        return@forEach
                    }

                    val nearestStop = stopMatcher.findNearestStop(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        stops = stops
                    )
                    val nearStop = nearestStop != null
                    val detectedTransport = stopMatcher.detectTransportType(nearestStop)

                    val nearestMetro = metroMatcher.isNearMetro(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        stations = metroEntrances
                    )
                    val nearestMcd = mcdMatcher.isNearMcd(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        stations = mcdEntrances
                    )

                    val nearMetro = nearestMetro != null
                    val nearMcd = nearestMcd != null

                    Log.d("STOP_MATCH", "nearestStop=${nearestStop?.stopName}")
                    Log.d("METRO_MATCH", nearestMetro?.stationName ?: "none")
                    Log.d("MCD_MATCH", nearestMcd?.stationName ?: "none")
                    Log.d("TRIP_DEBUG", "speedMps=$speed speedKmh=${speed * 3.6f}")

                    // All mutable state lives on the single-threaded serviceScope.
                    // Values captured here (speed, nearMetro, etc.) are immutable locals
                    // — safe to read from any thread.
                    serviceScope.launch {

                        // Update signal histories on the service thread (not main thread)
                        metroHistory.addLast(nearMetro)
                        if (metroHistory.size > 15) metroHistory.removeFirst()

                        mcdHistory.addLast(nearMcd)
                        if (mcdHistory.size > 15) mcdHistory.removeFirst()

                        // GPS accuracy tracking also lives here now
                        handleGpsSignal(location)

                        // Buffer points before trip is confirmed for retroactive flush on TripStarted
                        if (!tripDetector.isTripActive()) {
                            preTripBuffer.addLast(
                                BufferedPoint(timestamp, location.latitude, location.longitude, speed, location.accuracy)
                            )
                            val cutoff = timestamp - PRE_TRIP_BUFFER_MS
                            while (preTripBuffer.isNotEmpty() && preTripBuffer.first().timestamp < cutoff) {
                                preTripBuffer.removeFirst()
                            }
                        }

                        val event = tripDetector.process(
                            speedKmh = speed * 3.6f,
                            timestamp = timestamp
                        )

                        when (event) {
                            TripEvent.TripStarted -> {
                                Log.d("TRIP_DEBUG", "TRIP STARTED")
                                startTrip(timestamp, tripDetector.movementStartTime())
                            }
                            TripEvent.TripEnded -> {
                                Log.d("TRIP_DEBUG", "TRIP ENDED")
                                finishTrip(timestamp)
                            }
                            null -> Unit
                        }

                        if (tripDetector.isTripActive()) {

                            totalDistanceMeters += distance
                            speedSamples.add(speed)

                            val isWalking = walkDetector.isWalking(
                                speedMps = speed,
                                nearStop = nearStop,
                                nearMetro = nearMetro,
                                nearMcd = nearMcd
                            )

                            // GPS loss in a tunnel is a reliable metro signal
                            val gpsLost = currentGpsLossDuration() > Constants.GPS_LOSS_METRO_DURATION

                            // Latch metro once confirmed — keeps METRO votes through tunnel GPS noise
                            if (isStableMetroSignal() && speed < 25f) {
                                segmentMetroDetected = true
                                Log.d("METRO", "METRO latched via proximity")
                            }

                            val finalTransport = when {
                                gpsLost && speed < 20f -> {
                                    Log.d("METRO", "METRO via GPS loss")
                                    TransportType.METRO
                                }
                                segmentMetroDetected && speed < 25f -> {
                                    Log.d("METRO", "METRO via latched proximity")
                                    TransportType.METRO
                                }
                                isStableMcdSignal() && speed < 20f -> {
                                    Log.d("MCD", "MCD DETECTED")
                                    TransportType.MCD
                                }
                                isWalking -> TransportType.WALK
                                else -> detectedTransport
                            }

                            transportSamples.add(finalTransport)
                            Log.d("TRANSPORT_VOTE", finalTransport.name)
                            maybeUpdateNotification(finalTransport, speed * 3.6f)

                            // Transfer detection: sustained low speed seals the segment
                            if (speed < Constants.TRANSFER_SPEED_MPS) {
                                if (transferPauseStart == 0L) {
                                    transferPauseStart = timestamp
                                } else if (timestamp - transferPauseStart >= Constants.TRANSFER_PAUSE_DURATION) {
                                    sealCurrentSegment(timestamp)
                                    transferPauseStart = 0L
                                }
                            } else {
                                transferPauseStart = 0L
                            }
                        }

                        if (tripDetector.isTripActive()) {
                            saveGpsPoint(
                                location = location,
                                timestamp = timestamp,
                                speed = speed
                            )
                        }
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private suspend fun startTrip(confirmedTime: Long, movementStartTime: Long) {

        // Backdate trip to when movement first began, not when the 60-s window closed
        val actualStart = if (movementStartTime > 0L) movementStartTime else confirmedTime

        tripStartTime = actualStart
        segmentStartTime = actualStart
        speedSamples.clear()
        transportSamples.clear()
        completedSegments.clear()
        metroHistory.clear()
        mcdHistory.clear()
        segmentMetroDetected = false
        transferPauseStart = 0L
        totalDistanceMeters = 0f

        // endTime = 0 marks the trip as active; used for restart recovery
        currentTripId = repository.insertTrip(
            TripEntity(
                startTime = actualStart,
                endTime = 0L,
                transportType = "UNKNOWN",
                averageSpeed = 0f,
                dayType = "",
                timeBin = ""
            )
        )

        Log.d("TRIP_DEBUG", "Trip created id=$currentTripId actualStart=$actualStart confirmedAt=$confirmedTime")

        // Flush buffered pre-trip GPS points (exclude current tick — saveGpsPoint handles it)
        val tripId = currentTripId ?: return
        preTripBuffer
            .filter { it.timestamp >= actualStart && it.timestamp < confirmedTime }
            .forEach { pt ->
                repository.insertGpsPoint(
                    GpsPointEntity(
                        timestamp = pt.timestamp,
                        latitude = pt.latitude,
                        longitude = pt.longitude,
                        speed = pt.speed,
                        accuracy = pt.accuracy,
                        tripId = tripId
                    )
                )
            }
        preTripBuffer.clear()
    }

    // Finalises the in-progress segment, stores it in completedSegments, and
    // resets per-segment state + signal history so the next segment starts clean.
    private fun sealCurrentSegment(endTime: Long) {

        if (transportSamples.isEmpty()) return

        val movingSpeeds = speedSamples.filter { it > 1f }
        val avgSpeed = if (movingSpeeds.isNotEmpty()) movingSpeeds.average().toFloat() else 0f

        val segTransport = SegmentVoter.vote(transportSamples, avgSpeed)

        completedSegments.add(
            FinishedSegment(
                startTime = segmentStartTime,
                endTime = endTime,
                transportType = segTransport,
                averageSpeed = avgSpeed
            )
        )

        segmentStartTime = endTime
        transportSamples.clear()
        speedSamples.clear()
        metroHistory.clear()
        mcdHistory.clear()
        segmentMetroDetected = false

        Log.d("SEGMENT", "Sealed: $segTransport, total=${completedSegments.size}")
    }

    private suspend fun finishTrip(endTime: Long) {

        sealCurrentSegment(endTime)

        val primaryTransport = completedSegments
            .maxByOrNull { it.endTime - it.startTime }
            ?.transportType ?: TransportType.UNKNOWN

        val overallAvgSpeed = completedSegments
            .filter { it.averageSpeed > 0f }
            .map { it.averageSpeed }
            .let { if (it.isEmpty()) 0f else it.average().toFloat() }

        val dayType = analyzer.getDayType(tripStartTime)
        val timeBin = analyzer.getTimeBin(tripStartTime)

        currentTripId?.let { tripId ->

            repository.updateTrip(
                TripEntity(
                    id = tripId,
                    startTime = tripStartTime,
                    endTime = endTime,
                    transportType = primaryTransport.name,
                    averageSpeed = overallAvgSpeed,
                    dayType = dayType.name,
                    timeBin = timeBin.name,
                    distanceMeters = totalDistanceMeters
                )
            )

            // Only persist segments that weren't already in DB (e.g. after a restart)
            completedSegments.filter { !it.savedToDb }.forEach { seg ->
                repository.insertSegment(
                    TripSegmentEntity(
                        tripId = tripId,
                        startTime = seg.startTime,
                        endTime = seg.endTime,
                        transportType = seg.transportType.name,
                        averageSpeed = seg.averageSpeed
                    )
                )
            }

            Log.d("TRIP_DEBUG", "Trip finished with ${completedSegments.size} segment(s): $primaryTransport")
        }

        postTripCompletedNotification(primaryTransport, overallAvgSpeed, totalDistanceMeters, endTime - tripStartTime)
        updateWidgetData(primaryTransport, totalDistanceMeters, endTime - tripStartTime)

        resetTrip()
    }

    private fun postTripCompletedNotification(
        type: TransportType,
        avgSpeedMps: Float,
        distanceMeters: Float,
        durationMs: Long
    ) {
        val nm = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            Constants.TRIP_DONE_CHANNEL_ID,
            getString(R.string.notification_trip_done_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)

        val km = distanceMeters / 1000f
        val minutes = (durationMs / 60_000L).toInt()
        val text = getString(
            R.string.notification_trip_done_text,
            getString(type.nameResId()),
            km,
            minutes
        )

        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.TRIP_DONE_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_trip_done_title))
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .build()

        nm.notify(Constants.TRIP_DONE_NOTIFICATION_ID, notification)
    }

    private fun updateWidgetData(type: TransportType, distanceMeters: Float, durationMs: Long) {
        getSharedPreferences(Constants.WIDGET_PREFS, Context.MODE_PRIVATE).edit()
            .putString(Constants.WIDGET_KEY_TYPE, getString(type.nameResId()))
            .putFloat(Constants.WIDGET_KEY_DISTANCE, distanceMeters)
            .putLong(Constants.WIDGET_KEY_DURATION, durationMs)
            .apply()

        val ids = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, TripWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            TripWidgetProvider.updateAll(this)
        }
    }

    private fun resetTrip() {

        currentTripId = null
        tripStartTime = 0L
        segmentStartTime = 0L
        gpsLostStart = 0L
        gpsLostDuration = 0L
        totalDistanceMeters = 0f
        lastNotifiedTransport = TransportType.UNKNOWN
        speedSamples.clear()
        transportSamples.clear()
        completedSegments.clear()
        transferPauseStart = 0L
        preTripBuffer.clear()
        getSystemService(NotificationManager::class.java)
            .notify(Constants.NOTIFICATION_ID, buildNotification())
    }

    private suspend fun saveGpsPoint(
        location: Location,
        timestamp: Long,
        speed: Float
    ) {
        repository.insertGpsPoint(
            GpsPointEntity(
                timestamp = timestamp,
                latitude = location.latitude,
                longitude = location.longitude,
                speed = speed,
                accuracy = location.accuracy,
                tripId = currentTripId
            )
        )
    }

    private fun handleGpsSignal(location: Location) {

        if (location.accuracy > 50f) {
            if (gpsLostStart == 0L) gpsLostStart = System.currentTimeMillis()
        } else {
            if (gpsLostStart != 0L) {
                gpsLostDuration = System.currentTimeMillis() - gpsLostStart
                gpsLostStart = 0L
            }
        }
    }

    private fun currentGpsLossDuration(): Long {
        return if (gpsLostStart != 0L) System.currentTimeMillis() - gpsLostStart
        else gpsLostDuration
    }

    private fun isStableMetroSignal(): Boolean =
        metroHistory.count { it } >= 10

    private fun isStableMcdSignal(): Boolean =
        mcdHistory.count { it } >= 10

    private fun maybeUpdateNotification(transport: TransportType, speedKmh: Float) {
        if (transport == lastNotifiedTransport) return
        lastNotifiedTransport = transport
        getSystemService(NotificationManager::class.java)
            .notify(Constants.NOTIFICATION_ID, buildNotification(transport, speedKmh))
    }

    private fun buildNotification(
        transport: TransportType = TransportType.UNKNOWN,
        speedKmh: Float = 0f
    ): Notification {
        val text = if (transport != TransportType.UNKNOWN) {
            getString(R.string.notification_transport_speed, getString(transport.nameResId()), speedKmh.toInt())
        } else {
            getString(R.string.notification_tracking_active)
        }
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        TrackingState.isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
