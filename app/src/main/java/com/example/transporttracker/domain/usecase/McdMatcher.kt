package com.example.transporttracker.domain.usecase

import com.example.transporttracker.domain.model.McdEntrance

class McdMatcher {

    fun findNearestMcd(
        latitude: Double,
        longitude: Double,
        stations: List<McdEntrance>
    ): McdEntrance? {

        return stations.minByOrNull {

            distanceMeters(
                latitude,
                longitude,
                it.latitude,
                it.longitude
            )
        }
    }

    fun isNearMcd(
        latitude: Double,
        longitude: Double,
        stations: List<McdEntrance>,
        radiusMeters: Float = 300f
    ): McdEntrance? {

        val nearest =
            findNearestMcd(
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