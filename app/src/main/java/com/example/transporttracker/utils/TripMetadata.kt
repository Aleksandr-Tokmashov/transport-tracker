package com.example.transporttracker.utils

import java.util.Calendar

object TripMetadata {

    fun getDayType(
        timestamp: Long
    ): String {

        val calendar =
            Calendar.getInstance()

        calendar.timeInMillis = timestamp

        return when (
            calendar.get(Calendar.DAY_OF_WEEK)
        ) {

            Calendar.SATURDAY,
            Calendar.SUNDAY ->
                "WEEKEND"

            else ->
                "WEEKDAY"
        }
    }

    fun getTimeBin(
        timestamp: Long
    ): String {

        val calendar =
            Calendar.getInstance()

        calendar.timeInMillis = timestamp

        val hour =
            calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {

            in 6..11 ->
                "MORNING"

            in 12..17 ->
                "DAY"

            in 18..23 ->
                "EVENING"

            else ->
                "NIGHT"
        }
    }
}