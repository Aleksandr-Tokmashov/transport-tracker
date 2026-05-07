package com.example.transporttracker.utils

import android.annotation.SuppressLint
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.ui.trips.TripUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TripUiMapper {

    fun map(
        trip: Trip
    ): TripUiState {

        return TripUiState(

            id = trip.id,

            date =
                TripFormatter.formatDate(
                    trip.startTime
                ),

            startTime =
                TripFormatter.formatTimeOnly(
                    trip.startTime
                ),

            endTime =
                TripFormatter.formatTimeOnly(
                    trip.endTime
                ),

            duration =
                TripFormatter.formatDuration(
                    trip.startTime,
                    trip.endTime
                ),

            transportType =
                trip.transportType.name,

            averageSpeed =
                TripFormatter.formatSpeed(
                    trip.averageSpeed
                )
        )
    }
}