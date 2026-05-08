package com.example.transporttracker.domain.usecase

import android.location.Location
import com.example.transporttracker.domain.model.Stop

class StopMatcher {

    fun findNearestStop(
        latitude: Double,
        longitude: Double,
        stops: List<Stop>
    ): Stop? {

        return stops.minByOrNull { stop ->

            val results = FloatArray(1)

            Location.distanceBetween(
                latitude,
                longitude,
                stop.latitude,
                stop.longitude,
                results
            )

            results[0]
        }?.takeIf { stop ->

            val results = FloatArray(1)

            Location.distanceBetween(
                latitude,
                longitude,
                stop.latitude,
                stop.longitude,
                results
            )

            results[0] <= 100f
        }
    }
}