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
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.domain.model.MetroEntrance
import com.example.transporttracker.domain.model.Stop
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.MetroMatcher
import com.example.transporttracker.domain.usecase.StopMatcher
import com.example.transporttracker.domain.usecase.TripAnalyzer
import com.example.transporttracker.domain.usecase.TripDetector
import com.example.transporttracker.domain.usecase.TripEvent
import com.example.transporttracker.utils.AppContainer
import com.example.transporttracker.utils.Constants
import com.example.transporttracker.utils.LocationUtils
import com.example.transporttracker.utils.MetroParser
import com.example.transporttracker.utils.StopsParser
import com.google.android.gms.location.*
import kotlinx.coroutines.*

class LocationTrackingService : Service() {
    private lateinit var metroEntrances:
            List<MetroEntrance>

    private val metroMatcher =
        MetroMatcher()

    private lateinit var stops: List<Stop>

    private val stopMatcher =
        StopMatcher()

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )

    private lateinit var repository:
            TransportRepository

    private var currentTripId: Long? = null

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private lateinit var locationCallback:
            LocationCallback

    private val analyzer =
        TripAnalyzer()

    private val tripDetector =
        TripDetector()

    private var tripStartTime = 0L

    private val speedSamples =
        mutableListOf<Float>()

    private val transportSamples =
        mutableListOf<TransportType>()

    private var gpsLostStart = 0L

    private var gpsLostDuration = 0L

    private var lastLocation: Location? = null

    override fun onCreate() {
        super.onCreate()

        stops =
            StopsParser.parse(this)

        metroEntrances =
            MetroParser.parse(this)

        repository =
            AppContainer
                .provideRepository(applicationContext)

        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        createNotificationChannel()

        startForeground(
            Constants.NOTIFICATION_ID,
            createNotification()
        )

        startLocationUpdates()
    }

    private fun startLocationUpdates() {

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                Constants.LOCATION_INTERVAL
            )
                .setMinUpdateIntervalMillis(
                    Constants.LOCATION_FASTEST_INTERVAL
                )
                .build()

        locationCallback =
            object : LocationCallback() {

                override fun onLocationResult(
                    result: LocationResult
                ) {

                    result.locations.forEach { location ->

                        val timestamp =
                            location.time

                        val speed =
                            if (lastLocation != null) {

                                LocationUtils
                                    .calculateSpeedMps(
                                        lastLocation!!,
                                        location
                                    )

                            } else {
                                0f
                            }

                        lastLocation = location

                        // FILTER GPS SPIKES

                        if (speed > 60f) {

                            Log.d(
                                "GPS_FILTER",
                                "Ignored spike speed=$speed"
                            )

                            return@forEach
                        }

                        val nearestStop =
                            stopMatcher.findNearestStop(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                stops = stops
                            )

                        val detectedTransport =
                            stopMatcher.detectTransportType(
                                nearestStop
                            )

                        Log.d(
                            "TRANSPORT_TYPE",
                            detectedTransport.name
                        )

                        Log.d(
                            "STOP_MATCH",
                            "nearestStop=${nearestStop?.stopName}"
                        )

                        val nearestMetro =
                            metroMatcher.isNearMetro(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                stations = metroEntrances
                            )

                        Log.d(
                            "METRO_MATCH",
                            nearestMetro?.stationName ?: "none"
                        )

                        Log.d(
                            "TRIP_DEBUG",
                            "speedMps=$speed speedKmh=${speed * 3.6f}"
                        )

                        handleGpsSignal(location)

                        serviceScope.launch {

                            val event =
                                tripDetector.process(
                                    speedKmh = speed * 3.6f,
                                    timestamp = timestamp
                                )

                            when (event) {

                                TripEvent.TripStarted -> {

                                    Log.d(
                                        "TRIP_DEBUG",
                                        "TRIP STARTED"
                                    )

                                    startTrip(timestamp)
                                }

                                TripEvent.TripEnded -> {

                                    Log.d(
                                        "TRIP_DEBUG",
                                        "TRIP ENDED"
                                    )

                                    finishTrip(timestamp)
                                }

                                null -> Unit
                            }

                            if (tripDetector.isTripActive()) {

                                speedSamples.add(speed)

                                val finalTransport =
                                    if (
                                        nearestMetro != null &&
                                        getCurrentGpsLossDuration() > 60_000 &&
                                        speed * 3.6f > 10f
                                    ) {

                                        Log.d(
                                            "METRO",
                                            "METRO DETECTED"
                                        )

                                        TransportType.METRO

                                    } else {

                                        detectedTransport
                                    }

                                transportSamples.add(
                                    finalTransport
                                )

                                Log.d(
                                    "TRANSPORT_VOTE",
                                    finalTransport.name
                                )
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

            fusedLocationClient
                .requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    mainLooper
                )

        } catch (e: SecurityException) {

            e.printStackTrace()
        }
    }

    private suspend fun startTrip(
        startTime: Long
    ) {

        tripStartTime = startTime

        speedSamples.clear()

        transportSamples.clear()

        currentTripId =
            repository.insertTrip(
                TripEntity(
                    startTime = startTime,
                    endTime = startTime,
                    transportType = "UNKNOWN",
                    averageSpeed = 0f,
                    dayType = "",
                    timeBin = ""
                )
            )

        Log.d(
            "TRIP_DEBUG",
            "Trip created id=$currentTripId"
        )
    }

    private suspend fun saveGpsPoint(
        location: Location,
        timestamp: Long,
        speed: Float
    ) {

        val point =
            GpsPointEntity(
                timestamp = timestamp,
                latitude = location.latitude,
                longitude = location.longitude,
                speed = speed,
                accuracy = location.accuracy,
                tripId = currentTripId
            )

        repository.insertGpsPoint(point)
    }

    private fun handleGpsSignal(
        location: Location
    ) {

        if (location.accuracy > 50f) {

            if (gpsLostStart == 0L) {

                gpsLostStart =
                    System.currentTimeMillis()
            }

        } else {

            if (gpsLostStart != 0L) {

                gpsLostDuration =
                    System.currentTimeMillis() -
                            gpsLostStart

                gpsLostStart = 0L
            }
        }
    }

    private suspend fun finishTrip(
        endTime: Long
    ) {

        val movingSamples =
            speedSamples.filter {
                it > 1f
            }

        val averageSpeed =
            if (movingSamples.isNotEmpty()) {

                movingSamples
                    .average()
                    .toFloat()

            } else {
                0f
            }

        val transportType =
            transportSamples
                .groupBy { it }
                .maxByOrNull {
                    it.value.size
                }
                ?.key
                ?: TransportType.UNKNOWN

        val dayType =
            analyzer.getDayType(
                tripStartTime
            )

        val timeBin =
            analyzer.getTimeBin(
                tripStartTime
            )

        val trip =
            TripEntity(
                id = currentTripId ?: 0L,
                startTime = tripStartTime,
                endTime = endTime,
                transportType =
                    transportType.name,
                averageSpeed = averageSpeed,
                dayType = dayType.name,
                timeBin = timeBin.name
            )

        currentTripId?.let { tripId ->

            repository.updateTrip(
                trip.copy(id = tripId)
            )

            Log.d(
                "TRIP_DEBUG",
                "Trip updated=${trip.copy(id = tripId)}"
            )
        }

        resetTrip()
    }

    private fun resetTrip() {

        currentTripId = null

        tripStartTime = 0L

        gpsLostStart = 0L

        gpsLostDuration = 0L

        speedSamples.clear()

        transportSamples.clear()
    }

    private fun createNotification():
            Notification {

        return NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(
                "Transport Tracker"
            )
            .setContentText(
                "Tracking location..."
            )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {

        val channel =
            NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                "Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            )

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedLocationClient
            .removeLocationUpdates(
                locationCallback
            )

        serviceScope.cancel()
    }

    private fun getCurrentGpsLossDuration(): Long {

        return if (gpsLostStart != 0L) {

            System.currentTimeMillis() -
                    gpsLostStart

        } else {

            gpsLostDuration
        }
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}