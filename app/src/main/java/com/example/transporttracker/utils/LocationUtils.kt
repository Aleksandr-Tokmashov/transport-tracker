package com.example.transporttracker.utils

import android.location.Location

object LocationUtils {

    fun calculateSpeedMps(
        oldLocation: Location,
        newLocation: Location
    ): Float {

        val distance =
            oldLocation.distanceTo(newLocation)

        val timeSeconds =
            (newLocation.time - oldLocation.time) / 1000f

        if (timeSeconds <= 0f) return 0f

        return distance / timeSeconds
    }
}