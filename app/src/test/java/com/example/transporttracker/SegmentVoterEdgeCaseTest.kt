package com.example.transporttracker

import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.SegmentVoter
import org.junit.Assert.assertEquals
import org.junit.Test

class SegmentVoterEdgeCaseTest {

    @Test
    fun tramMajority_returnsTram() {
        val samples = listOf(
            TransportType.TRAM, TransportType.TRAM, TransportType.TRAM,
            TransportType.BUS, TransportType.UNKNOWN
        )
        assertEquals(TransportType.TRAM, SegmentVoter.vote(samples, avgSpeedMps = 4f))
    }

    @Test
    fun walkMajority_returnsWalk() {
        val samples = listOf(
            TransportType.WALK, TransportType.WALK,
            TransportType.BUS
        )
        assertEquals(TransportType.WALK, SegmentVoter.vote(samples, avgSpeedMps = 1f))
    }

    @Test
    fun unknownWithMetroSpeed_overridesToMetro() {
        // UNKNOWN at 10+ m/s → GPS noise underground → METRO
        val samples = listOf(TransportType.UNKNOWN, TransportType.UNKNOWN)
        assertEquals(TransportType.METRO, SegmentVoter.vote(samples, avgSpeedMps = 11f))
    }

    @Test
    fun busWithMetroSpeed_overridesToMetro() {
        // BUS winner but metro-level speed → likely underground GPS drift
        val samples = listOf(TransportType.BUS, TransportType.BUS, TransportType.BUS)
        assertEquals(TransportType.METRO, SegmentVoter.vote(samples, avgSpeedMps = 11f))
    }

    @Test
    fun metroWinnerWithMetroSpeed_remainsMetro() {
        val samples = listOf(TransportType.METRO, TransportType.METRO)
        assertEquals(TransportType.METRO, SegmentVoter.vote(samples, avgSpeedMps = 12f))
    }

    @Test
    fun unknownAtExactlyBusFallbackThreshold_remainsUnknown() {
        // BUS_FALLBACK_SPEED_MPS = 3.5; exactly 3.5 is NOT > 3.5
        val samples = listOf(TransportType.UNKNOWN)
        assertEquals(TransportType.UNKNOWN, SegmentVoter.vote(samples, avgSpeedMps = 3.5f))
    }

    @Test
    fun unknownJustAboveBusFallbackThreshold_becomesBus() {
        val samples = listOf(TransportType.UNKNOWN)
        assertEquals(TransportType.BUS, SegmentVoter.vote(samples, avgSpeedMps = 3.6f))
    }

    @Test
    fun tramWinnerWithMetroSpeed_isNotOverridden() {
        // TRAM is neither BUS nor UNKNOWN, so metro override does not apply
        val samples = listOf(TransportType.TRAM, TransportType.TRAM, TransportType.TRAM)
        assertEquals(TransportType.TRAM, SegmentVoter.vote(samples, avgSpeedMps = 12f))
    }

    @Test
    fun singleSample_returnsIt() {
        assertEquals(TransportType.MCD, SegmentVoter.vote(listOf(TransportType.MCD), 5f))
    }
}
