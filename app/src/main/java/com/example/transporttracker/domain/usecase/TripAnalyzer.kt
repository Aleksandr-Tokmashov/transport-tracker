package com.example.transporttracker.domain.usecase

import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import java.util.Calendar

class TripAnalyzer {

    companion object {

        const val START_SPEED = 1.94f
        const val STOP_SPEED = 0.83f

        const val MIN_TRIP_DURATION = 60_000L
        const val STOP_DURATION = 120_000L
    }

    fun determineTransportType(
        averageSpeed: Float,
        gpsLostDuration: Long
    ): TransportType {

        val speedKmh = averageSpeed * 3.6f

        return when {

            gpsLostDuration > 120_000L &&
                    speedKmh > 15f -> {
                TransportType.METRO
            }

            speedKmh < 7f -> {
                TransportType.WALK
            }

            speedKmh in 7f..40f -> {
                TransportType.BUS
            }

            else -> {
                TransportType.UNKNOWN
            }
        }
    }

    fun getDayType(timestamp: Long): DayType {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = timestamp

        return when (
            calendar.get(Calendar.DAY_OF_WEEK)
        ) {
            Calendar.SATURDAY,
            Calendar.SUNDAY -> DayType.WEEKEND

            else -> DayType.WEEKDAY
        }
    }

    fun getTimeBin(timestamp: Long): TimeBin {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = timestamp

        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {

            in 6..11 -> TimeBin.MORNING

            in 12..17 -> TimeBin.DAY

            in 18..23 -> TimeBin.EVENING

            else -> TimeBin.NIGHT
        }
    }

    fun createTripEntity(
        startTime: Long,
        endTime: Long,
        averageSpeed: Float,
        gpsLostDuration: Long,
        dayType: DayType,
        timeBin: TimeBin
    ): TripEntity {

        return TripEntity(

            startTime = startTime,

            endTime = endTime,

            transportType =
                determineTransportType(
                    averageSpeed = averageSpeed,
                    gpsLostDuration = gpsLostDuration
                ).name,

            averageSpeed = averageSpeed,

            dayType = dayType.name,

            timeBin = timeBin.name
        )
    }
}