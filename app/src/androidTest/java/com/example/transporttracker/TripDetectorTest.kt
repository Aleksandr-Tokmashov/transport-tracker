package com.example.transporttracker

import com.example.transporttracker.domain.usecase.TripDetector
import com.example.transporttracker.domain.usecase.TripEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TripDetectorTest {

    private lateinit var detector: TripDetector

    @Before
    fun setup() {

        detector = TripDetector()
    }

    @Test
    fun detectsTripStartAfter60Seconds() {

        val startTime = 0L

        val first =
            detector.process(
                speedKmh = 10f,
                timestamp = startTime
            )

        assertNull(first)

        val second =
            detector.process(
                speedKmh = 10f,
                timestamp = 61_000L
            )

        assertEquals(
            TripEvent.TripStarted,
            second
        )
    }

    @Test
    fun doesNotStartTripTooEarly() {

        val result =
            detector.process(
                speedKmh = 10f,
                timestamp = 30_000L
            )

        assertNull(result)
    }

    @Test
    fun detectsTripEndAfterStopDuration() {

        detector.process(
            speedKmh = 10f,
            timestamp = 0L
        )

        detector.process(
            speedKmh = 10f,
            timestamp = 61_000L
        )

        val result =
            detector.process(
                speedKmh = 1f,
                timestamp = 190_000L
            )

        assertEquals(
            TripEvent.TripEnded,
            result
        )
    }

    @Test
    fun doesNotEndTripImmediately() {

        detector.process(
            speedKmh = 10f,
            timestamp = 0L
        )

        detector.process(
            speedKmh = 10f,
            timestamp = 61_000L
        )

        val result =
            detector.process(
                speedKmh = 1f,
                timestamp = 100_000L
            )

        assertNull(result)
    }
}