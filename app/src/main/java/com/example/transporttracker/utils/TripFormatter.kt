package com.example.transporttracker.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object TripFormatter {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))

    fun formatTimeOnly(timestamp: Long): String = timeFormatter.format(Date(timestamp))

    fun formatDuration(startTime: Long, endTime: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime)
        return "$minutes мин"
    }

    @SuppressLint("DefaultLocale")
    fun formatSpeed(speedMps: Float): String {
        val kmh = speedMps * 3.6f
        return String.format("%.1f км/ч", kmh)
    }

    fun formatDistance(meters: Float): String {
        return if (meters < 1000f) {
            "${meters.roundToInt()} м"
        } else {
            val km = meters / 1000f
            if (km < 10f) String.format("%.1f км", km) else "${km.roundToInt()} км"
        }
    }
}
