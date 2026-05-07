package com.example.transporttracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TripFormatter {

    private val dateFormatter =
        SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        )

    private val timeFormatter =
        SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        )

    fun formatDate(
        timestamp: Long
    ): String {

        return dateFormatter.format(
            Date(timestamp)
        )
    }

    fun formatTimeOnly(
        timestamp: Long
    ): String {

        return timeFormatter.format(
            Date(timestamp)
        )
    }

    @SuppressLint("DefaultLocale")
    fun formatDuration(
        startTime: Long,
        endTime: Long
    ): String {

        val duration =
            endTime - startTime

        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(duration)

        return "$minutes min"
    }

    @SuppressLint("DefaultLocale")
    fun formatSpeed(
        speedMps: Float
    ): String {

        val kmh = speedMps * 3.6f

        return String.format(
            "%.1f km/h",
            kmh
        )
    }
}