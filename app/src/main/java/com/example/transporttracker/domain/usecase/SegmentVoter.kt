package com.example.transporttracker.domain.usecase

import com.example.transporttracker.domain.model.TransportType

object SegmentVoter {

    // Above this speed an UNKNOWN segment is reclassified as BUS (~12.6 km/h)
    private const val BUS_FALLBACK_SPEED_MPS = 3.5f

    fun vote(
        samples: List<TransportType>,
        avgSpeedMps: Float
    ): TransportType {

        val winner = samples
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: TransportType.UNKNOWN

        // No stop matched within 100 m but speed is clearly vehicular → BUS
        return if (winner == TransportType.UNKNOWN && avgSpeedMps > BUS_FALLBACK_SPEED_MPS) {
            TransportType.BUS
        } else {
            winner
        }
    }
}
