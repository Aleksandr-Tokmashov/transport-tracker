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
import com.example.transporttracker.domain.usecase.TripAnalyzer
import com.example.transporttracker.utils.AppContainer
import com.example.transporttracker.utils.Constants
import com.example.transporttracker.utils.LocationUtils
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient:
            FusedLocationProviderClient

    private lateinit var locationCallback:
            LocationCallback

    private val analyzer = TripAnalyzer()

    // Trip state

    private var isTripActive = false

    private var tripStartTime = 0L

    private var lastMovingTime = 0L

    private val speedSamples =
        mutableListOf<Float>()

    private var gpsLostStart = 0L

    private var gpsLostDuration = 0L

    // Previous location for manual speed calculation

    private var lastLocation: Location? = null

    override fun onCreate() {
        super.onCreate()

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

                        // Manual speed calculation

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

                        Log.d(
                            "TRIP_DEBUG",
                            "speed=$speed"
                        )

                        saveGpsPoint(
                            location = location,
                            timestamp = timestamp,
                            speed = speed
                        )

                        handleGpsSignal(location)

                        handleTripLogic(
                            speed = speed,
                            timestamp = timestamp
                        )
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

    private fun saveGpsPoint(
        location: Location,
        timestamp: Long,
        speed: Float
    ) {

        val point = GpsPointEntity(
            timestamp = timestamp,
            latitude = location.latitude,
            longitude = location.longitude,
            speed = speed,
            accuracy = location.accuracy
        )

        CoroutineScope(Dispatchers.IO).launch {

            AppContainer
                .provideRepository(applicationContext)
                .insertGpsPoint(point)
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

    private fun handleTripLogic(
        speed: Float,
        timestamp: Long
    ) {

        // Start trip

        if (speed > TripAnalyzer.START_SPEED) {

            if (!isTripActive) {

                if (tripStartTime == 0L) {

                    tripStartTime = timestamp

                    Log.d(
                        "TRIP_DEBUG",
                        "Potential trip started"
                    )
                }

                if (
                    timestamp - tripStartTime >
                    TripAnalyzer.MIN_TRIP_DURATION
                ) {

                    isTripActive = true

                    Log.d(
                        "TRIP_DEBUG",
                        "TRIP ACTIVE"
                    )
                }
            }

            lastMovingTime = timestamp

            speedSamples.add(speed)
        }

        // Stop trip

        else if (isTripActive) {

            if (
                timestamp - lastMovingTime >
                TripAnalyzer.STOP_DURATION
            ) {

                Log.d(
                    "TRIP_DEBUG",
                    "TRIP FINISHED"
                )

                finishTrip(timestamp)
            }
        }
    }

    private fun finishTrip(
        endTime: Long
    ) {

        val averageSpeed =
            if (speedSamples.isNotEmpty()) {

                speedSamples.average().toFloat()

            } else {
                0f
            }

        val trip =
            analyzer.createTripEntity(
                startTime = tripStartTime,
                endTime = endTime,
                averageSpeed = averageSpeed,
                gpsLostDuration = gpsLostDuration
            )

        CoroutineScope(Dispatchers.IO).launch {

            AppContainer
                .provideRepository(applicationContext)
                .insertTrip(trip)

            Log.d(
                "TRIP_DEBUG",
                "Trip saved to database"
            )
        }

        resetTrip()
    }

    private fun resetTrip() {

        isTripActive = false

        tripStartTime = 0L

        lastMovingTime = 0L

        gpsLostStart = 0L

        gpsLostDuration = 0L

        speedSamples.clear()
    }

    private fun createNotification():
            Notification {

        return NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle("Transport Tracker")
            .setContentText("Tracking location...")
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
            .removeLocationUpdates(locationCallback)
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null
}