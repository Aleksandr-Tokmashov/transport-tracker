package com.example.transporttracker.domain.usecase

import com.example.transporttracker.domain.model.TransportType

object SegmentVoter {

    // Above this speed an UNKNOWN segment is reclassified as BUS (~12.6 km/h)
    private const val BUS_FALLBACK_SPEED_MPS = 3.5f

    // Above this speed a BUS/UNKNOWN result is overridden to METRO (~36 km/h).
    // Regular city buses rarely sustain this average; metro easily does.
    private const val METRO_SPEED_THRESHOLD_MPS = 10f

    fun vote(
        samples: List<TransportType>,
        avgSpeedMps: Float
    ): TransportType {

        val winner = samples
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: TransportType.UNKNOWN

        // BUS or UNKNOWN at metro-level speed → GPS noise washed out the metro signal underground
        if (avgSpeedMps > METRO_SPEED_THRESHOLD_MPS &&
            (winner == TransportType.UNKNOWN || winner == TransportType.BUS)
        ) {
            return TransportType.METRO
        }

        // No stop matched within 100 m but speed is clearly vehicular → BUS
        return if (winner == TransportType.UNKNOWN && avgSpeedMps > BUS_FALLBACK_SPEED_MPS) {
            TransportType.BUS
        } else {
            winner
        }
    }
}
