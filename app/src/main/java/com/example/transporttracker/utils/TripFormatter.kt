package com.example.transporttracker.utils

import android.annotation.SuppressLint
import android.content.Context
import com.example.transporttracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class TripFormatter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))

    fun formatTimeOnly(timestamp: Long): String = timeFormatter.format(Date(timestamp))

    fun formatDuration(startTime: Long, endTime: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime)
        return context.getString(R.string.duration_minutes, minutes.toInt())
    }

    @SuppressLint("DefaultLocale")
    fun formatSpeed(speedMps: Float): String {
        val kmh = speedMps * 3.6f
        return context.getString(R.string.speed_kmh, kmh)
    }

    fun formatDistance(meters: Float): String = when {
        meters < 1000f -> context.getString(R.string.distance_meters, meters.roundToInt())
        meters / 1000f < 10f -> context.getString(R.string.distance_km_decimal, meters / 1000f)
        else -> context.getString(R.string.distance_km, (meters / 1000f).roundToInt())
    }
}
