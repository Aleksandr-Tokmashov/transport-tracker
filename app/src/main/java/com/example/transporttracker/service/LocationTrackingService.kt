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
import com.example.transporttracker.domain.model.Stop
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.StopMatcher
import com.example.transporttracker.domain.usecase.TripAnalyzer
import com.example.transporttracker.domain.usecase.TripDetector
import com.example.transporttracker.domain.usecase.TripEvent
import com.example.transporttracker.utils.AppContainer
import com.example.transporttracker.utils.Constants
import com.example.transporttracker.utils.LocationUtils
import com.example.transporttracker.utils.StopsParser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {
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

    private val analyzer = TripAnalyzer()

    private val tripDetector = TripDetector()

    private var tripStartTime = 0L

    private val speedSamples =
        mutableListOf<Float>()

    private var gpsLostStart = 0L

    private var gpsLostDuration = 0L

    private var lastLocation: Location? = null

    private var detectedTransport =
        TransportType.UNKNOWN

    override fun onCreate() {
        super.onCreate()

        stops =
            StopsParser.parse(this)

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
                            System.currentTimeMillis()

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

                        val nearestStop =
                            stopMatcher.findNearestStop(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                stops = stops
                            )

                        detectedTransport =
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

                        Log.d(
                            "TRIP_DEBUG",
                            "speed=$speed"
                        )

                        if (currentTripId != null) {

                            speedSamples.add(speed)
                        }

                        saveGpsPoint(
                            location = location,
                            timestamp = timestamp,
                            speed = speed
                        )

                        handleGpsSignal(location)

                        val event =
                            tripDetector.process(
                                speedKmh = speed * 3.6f,
                                timestamp = timestamp
                            )

                        if (tripDetector.isTripActive()) {
                            speedSamples.add(speed)
                        }

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

    private fun startTrip(
        startTime: Long
    ) {

        tripStartTime = startTime

        speedSamples.clear()

        serviceScope.launch {

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
    }

    private fun saveGpsPoint(
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

        serviceScope.launch {

            repository.insertGpsPoint(point)
        }
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

    private fun finishTrip(
        endTime: Long
    ) {

        val averageSpeed =
            if (speedSamples.isNotEmpty()) {

                speedSamples
                    .average()
                    .toFloat()

            } else {
                0f
            }

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
                    detectedTransport.name,
                averageSpeed = averageSpeed,
                dayType = dayType.name,
                timeBin = timeBin.name
            )

        serviceScope.launch {

            currentTripId?.let { tripId ->

                val updatedTrip =
                    trip.copy(id = tripId)

                repository.updateTrip(
                    updatedTrip
                )

                Log.d(
                    "TRIP_DEBUG",
                    "Trip updated=$updatedTrip"
                )
            }

            resetTrip()
        }
    }

    private fun resetTrip() {

        currentTripId = null

        tripStartTime = 0L

        gpsLostStart = 0L

        gpsLostDuration = 0L

        speedSamples.clear()

        detectedTransport =
            TransportType.UNKNOWN
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

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}
