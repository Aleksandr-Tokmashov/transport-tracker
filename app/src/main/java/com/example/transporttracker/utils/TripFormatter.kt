package com.example.transporttracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TripFormatter {

    private val dateFormat =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        )

    fun formatDate(
        timestamp: Long
    ): String {

        return dateFormat.format(
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

        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(duration) % 60

        return String.format(
            "%02d:%02d",
            minutes,
            seconds
        )
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