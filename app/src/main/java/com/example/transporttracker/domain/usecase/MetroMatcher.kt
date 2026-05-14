package com.example.transporttracker.domain.usecase

import com.example.transporttracker.domain.model.MetroEntrance
import kotlin.math.*

class MetroMatcher {

    fun findNearestMetro(
        latitude: Double,
        longitude: Double,
        stations: List<MetroEntrance>
    ): MetroEntrance? {

        return stations.minByOrNull {

            distanceMeters(
                latitude,
                longitude,
                it.latitude,
                it.longitude
            )
        }
    }

    fun isNearMetro(
        latitude: Double,
        longitude: Double,
        stations: List<MetroEntrance>,
        radiusMeters: Float = 70f
    ): MetroEntrance? {

        val nearest =
            findNearestMetro(
                latitude,
                longitude,
                stations
            )

        nearest ?: return null

        val distance =
            distanceMeters(
                latitude,
                longitude,
                nearest.latitude,
                nearest.longitude
            )

        return if (distance <= radiusMeters) {
            nearest
        } else {
            null
        }
    }

    private fun distanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {

        val results = FloatArray(1)

        android.location.Location.distanceBetween(
            lat1,
            lon1,
            lat2,
            lon2,
            results
        )

        return results[0]
    }
}