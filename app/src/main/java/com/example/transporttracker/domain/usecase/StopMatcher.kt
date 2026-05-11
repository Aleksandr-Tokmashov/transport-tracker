package com.example.transporttracker.domain.usecase

import android.location.Location
import com.example.transporttracker.domain.model.Stop
import com.example.transporttracker.domain.model.TransportType

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

    fun detectTransportType(
        stop: Stop?
    ): TransportType {

        if (stop == null) {
            return TransportType.UNKNOWN
        }

        return when {

            stop.transportType.contains(
                "автобус",
                ignoreCase = true
            ) -> TransportType.BUS

            stop.transportType.contains(
                "трамвай",
                ignoreCase = true
            ) -> TransportType.TRAM

            else -> TransportType.UNKNOWN
        }
    }
}