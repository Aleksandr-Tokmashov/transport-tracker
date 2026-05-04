package com.example.transporttracker.utils

import android.annotation.SuppressLint
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.ui.trips.TripUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TripUiMapper {

    @SuppressLint("ConstantLocale")
    private val dateFormatter =
        SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        )

    @SuppressLint("ConstantLocale")
    private val timeFormatter =
        SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        )

    fun map(
        trip: TripEntity
    ): TripUiState {

        val durationMillis =
            trip.endTime - trip.startTime

        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(
                durationMillis
            )

        return TripUiState(

            id = trip.id,

            date =
                dateFormatter.format(
                    Date(trip.startTime)
                ),

            startTime =
                timeFormatter.format(
                    Date(trip.startTime)
                ),

            endTime =
                timeFormatter.format(
                    Date(trip.endTime)
                ),

            duration = "$minutes min",

            transportType = trip.transportType,

            averageSpeed =
                "${trip.averageSpeed.toInt()} km/h"
        )
    }
}