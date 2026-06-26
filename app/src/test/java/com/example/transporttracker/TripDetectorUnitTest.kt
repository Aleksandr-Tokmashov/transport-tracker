package com.example.transporttracker

import com.example.transporttracker.domain.usecase.TripDetector
import com.example.transporttracker.domain.usecase.TripEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TripDetectorUnitTest {

    private lateinit var detector: TripDetector

    @Before
    fun setup() {
        detector = TripDetector()
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun initiallyNotActive() {
        assertFalse(detector.isTripActive())
    }

    @Test
    fun movementStartTime_isMinusOneBeforeAnyInput() {
        assertEquals(-1L, detector.movementStartTime())
    }

    // ── trip start detection ──────────────────────────────────────────────────

    @Test
    fun firstHighSpeedSample_doesNotStartTrip() {
        val event = detector.process(speedKmh = 10f, timestamp = 0L)
        assertNull(event)
        assertFalse(detector.isTripActive())
    }

    @Test
    fun highSpeedFor59Seconds_doesNotStartTrip() {
        detector.process(speedKmh = 10f, timestamp = 0L)
        val event = detector.process(speedKmh = 10f, timestamp = 59_000L)
        assertNull(event)
    }

    @Test
    fun highSpeedForExactly60Seconds_startsTrip() {
        detector.process(speedKmh = 10f, timestamp = 0L)
        val event = detector.process(speedKmh = 10f, timestamp = 60_000L)
        assertEquals(TripEvent.TripStarted, event)
        assertTrue(detector.isTripActive())
    }

    @Test
    fun speedDropBelowThreshold_resetsPotentialStart() {
        detector.process(speedKmh = 10f, timestamp = 0L)
        detector.process(speedKmh = 3f, timestamp = 50_000L)  // drop — should reset
        // Subsequent high-speed sample restarts the 60-second window
        detector.process(speedKmh = 10f, timestamp = 51_000L)
        val event = detector.process(speedKmh = 10f, timestamp = 100_000L)
        // 100_000 - 51_000 = 49s < 60s → not yet started
        assertNull(event)
    }

    @Test
    fun movementStartTime_capturesFirstHighSpeedTimestamp() {
        detector.process(speedKmh = 10f, timestamp = 12_000L)
        assertEquals(12_000L, detector.movementStartTime())
    }

    @Test
    fun movementStartTime_resetOnSpeedDrop() {
        detector.process(speedKmh = 10f, timestamp = 12_000L)
        detector.process(speedKmh = 3f, timestamp = 20_000L)  // below START_SPEED
        assertEquals(-1L, detector.movementStartTime())
    }

    @Test
    fun speedExactlyAtStartThreshold_doesNotStartWindow() {
        // START_SPEED = 7 km/h; exactly 7 is NOT > 7
        val event = detector.process(speedKmh = 7f, timestamp = 0L)
        assertNull(event)
        assertEquals(-1L, detector.movementStartTime())
    }

    @Test
    fun speedJustAboveStartThreshold_opensWindow() {
        detector.process(speedKmh = 7.1f, timestamp = 0L)
        assertEquals(0L, detector.movementStartTime())
    }

    // ── trip end detection ────────────────────────────────────────────────────

    private fun startTrip() {
        detector.process(speedKmh = 10f, timestamp = 0L)
        detector.process(speedKmh = 10f, timestamp = 61_000L)  // TripStarted
    }

    @Test
    fun slowSpeedFor119Seconds_doesNotEndTrip() {
        startTrip()
        val event = detector.process(speedKmh = 1f, timestamp = 180_000L)
        assertNull(event)
    }

    @Test
    fun slowSpeedForExactly120Seconds_endsTrip() {
        startTrip()
        detector.process(speedKmh = 1f, timestamp = 61_001L)   // first slow tick
        val event = detector.process(speedKmh = 1f, timestamp = 181_001L)  // +120s
        assertEquals(TripEvent.TripEnded, event)
        assertFalse(detector.isTripActive())
    }

    @Test
    fun speedRecoveryDuringStopWindow_preventsEnd() {
        startTrip()
        detector.process(speedKmh = 1f, timestamp = 61_001L)   // slow starts
        detector.process(speedKmh = 20f, timestamp = 100_000L) // moved again
        val event = detector.process(speedKmh = 1f, timestamp = 200_000L) // slow again
        // 200_000 - 100_000 = 100s < 120s → not ended
        assertNull(event)
    }

    @Test
    fun afterTripEnds_newTripCanStart() {
        startTrip()
        detector.process(speedKmh = 1f, timestamp = 61_000L)
        detector.process(speedKmh = 1f, timestamp = 200_000L)  // TripEnded

        // Start a second trip
        detector.process(speedKmh = 10f, timestamp = 300_000L)
        val event = detector.process(speedKmh = 10f, timestamp = 365_000L)
        assertEquals(TripEvent.TripStarted, event)
    }

    // ── forceActive ───────────────────────────────────────────────────────────

    @Test
    fun forceActive_makesDetectorActive() {
        assertFalse(detector.isTripActive())
        detector.forceActive()
        assertTrue(detector.isTripActive())
    }

    @Test
    fun forceActive_allowsNormalTripEnd() {
        detector.forceActive()
        val now = System.currentTimeMillis()
        detector.process(speedKmh = 1f, timestamp = now)
        val event = detector.process(speedKmh = 1f, timestamp = now + 121_000L)
        assertEquals(TripEvent.TripEnded, event)
    }
}
