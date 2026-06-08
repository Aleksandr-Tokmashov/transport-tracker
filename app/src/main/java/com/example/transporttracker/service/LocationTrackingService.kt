package com.example.transporttracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
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
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

    // -------------------------------------------------------------------------

    override fun onCreate() {
        super.onCreate()

        TrackingState.isTracking = true

        stops = StopsParser.parse(this)
        metroEntrances = MetroParser.parse(this)
        mcdEntrances = McdParser.parse(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()
        startForeground(Constants.NOTIFICATION_ID, createNotification())

        // Restore any trip that was interrupted when the service was killed
        serviceScope.launch {
            val activeTrip = repository.getActiveTrip()
            if (activeTrip != null) {
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

                    lastLocation = location

                    // Filter GPS spikes
                    if (speed > 60f) {
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

                    metroHistory.addLast(nearMetro)
                    if (metroHistory.size > 15) metroHistory.removeFirst()

                    mcdHistory.addLast(nearMcd)
                    if (mcdHistory.size > 15) mcdHistory.removeFirst()

                    Log.d("STOP_MATCH", "nearestStop=${nearestStop?.stopName}")
                    Log.d("METRO_MATCH", nearestMetro?.stationName ?: "none")
                    Log.d("MCD_MATCH", nearestMcd?.stationName ?: "none")
                    Log.d("TRIP_DEBUG", "speedMps=$speed speedKmh=${speed * 3.6f}")

                    handleGpsSignal(location)

                    serviceScope.launch {

                        val event = tripDetector.process(
                            speedKmh = speed * 3.6f,
                            timestamp = timestamp
                        )

                        when (event) {
                            TripEvent.TripStarted -> {
                                Log.d("TRIP_DEBUG", "TRIP STARTED")
                                startTrip(timestamp)
                            }
                            TripEvent.TripEnded -> {
                                Log.d("TRIP_DEBUG", "TRIP ENDED")
                                finishTrip(timestamp)
                            }
                            null -> Unit
                        }

                        if (tripDetector.isTripActive()) {

                            speedSamples.add(speed)

                            val isWalking = walkDetector.isWalking(
                                speedMps = speed,
                                nearStop = nearStop,
                                nearMetro = nearMetro,
                                nearMcd = nearMcd
                            )

                            // GPS loss in a tunnel is a reliable metro signal
                            val gpsLost = currentGpsLossDuration() > Constants.GPS_LOSS_METRO_DURATION

                            val finalTransport = when {
                                gpsLost && speed < 20f -> {
                                    Log.d("METRO", "METRO via GPS loss")
                                    TransportType.METRO
                                }
                                isStableMetroSignal() && speed < 12f -> {
                                    Log.d("METRO", "METRO via proximity")
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

                        saveGpsPoint(
                            location = location,
                            timestamp = timestamp,
                            speed = speed
                        )
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

    private suspend fun startTrip(startTime: Long) {

        tripStartTime = startTime
        segmentStartTime = startTime
        speedSamples.clear()
        transportSamples.clear()
        completedSegments.clear()
        metroHistory.clear()
        mcdHistory.clear()
        transferPauseStart = 0L

        // endTime = 0 marks the trip as active; used for restart recovery
        currentTripId = repository.insertTrip(
            TripEntity(
                startTime = startTime,
                endTime = 0L,
                transportType = "UNKNOWN",
                averageSpeed = 0f,
                dayType = "",
                timeBin = ""
            )
        )

        Log.d("TRIP_DEBUG", "Trip created id=$currentTripId")
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
                    timeBin = timeBin.name
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

        resetTrip()
    }

    private fun resetTrip() {

        currentTripId = null
        tripStartTime = 0L
        segmentStartTime = 0L
        gpsLostStart = 0L
        gpsLostDuration = 0L
        speedSamples.clear()
        transportSamples.clear()
        completedSegments.clear()
        transferPauseStart = 0L
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

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Transport Tracker")
            .setContentText("Tracking location...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            "Tracking Service",
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
