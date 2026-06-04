package com.example.transporttracker

import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.SegmentVoter
import org.junit.Assert.assertEquals
import org.junit.Test

class SegmentVoterTest {

    @Test
    fun clearMajority_returnsWinner() {
        val samples = listOf(
            TransportType.BUS,
            TransportType.BUS,
            TransportType.BUS,
            TransportType.METRO,
            TransportType.WALK
        )
        assertEquals(TransportType.BUS, SegmentVoter.vote(samples, avgSpeedMps = 5f))
    }

    @Test
    fun metroMajority_returnsMetro() {
        val samples = listOf(
            TransportType.METRO,
            TransportType.METRO,
            TransportType.METRO,
            TransportType.BUS,
            TransportType.UNKNOWN
        )
        assertEquals(TransportType.METRO, SegmentVoter.vote(samples, avgSpeedMps = 8f))
    }

    @Test
    fun unknownWithVehicularSpeed_returnsBusFallback() {
        val samples = listOf(
            TransportType.UNKNOWN,
            TransportType.UNKNOWN,
            TransportType.UNKNOWN
        )
        assertEquals(TransportType.BUS, SegmentVoter.vote(samples, avgSpeedMps = 5f))
    }

    @Test
    fun unknownWithLowSpeed_remainsUnknown() {
        val samples = listOf(
            TransportType.UNKNOWN,
            TransportType.UNKNOWN,
            TransportType.UNKNOWN
        )
        assertEquals(TransportType.UNKNOWN, SegmentVoter.vote(samples, avgSpeedMps = 2f))
    }

    @Test
    fun emptyList_returnsUnknown() {
        assertEquals(TransportType.UNKNOWN, SegmentVoter.vote(emptyList(), avgSpeedMps = 0f))
    }

    @Test
    fun mcdMajority_returnsMcd() {
        val samples = listOf(
            TransportType.MCD,
            TransportType.MCD,
            TransportType.BUS
        )
        assertEquals(TransportType.MCD, SegmentVoter.vote(samples, avgSpeedMps = 12f))
    }

    @Test
    fun tiedVote_returnsFirstByIteration() {
        // Tied between BUS and METRO — result is deterministic (whichever Map.maxByOrNull picks)
        val samples = listOf(TransportType.BUS, TransportType.METRO)
        val result = SegmentVoter.vote(samples, avgSpeedMps = 5f)
        assert(result == TransportType.BUS || result == TransportType.METRO)
    }
}
