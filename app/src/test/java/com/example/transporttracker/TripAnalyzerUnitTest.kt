package com.example.transporttracker

import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.TripAnalyzer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.TimeZone

/**
 * All timestamps use UTC so results are timezone-independent.
 * Jan 1 2025 UTC = 1735689600000L (Wednesday)
 */
class TripAnalyzerUnitTest {

    private val analyzer = TripAnalyzer()
    private lateinit var savedTz: TimeZone

    @Before
    fun forceUtc() {
        savedTz = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreTz() {
        TimeZone.setDefault(savedTz)
    }

    // ── getDayType ────────────────────────────────────────────────────────────

    @Test
    fun saturday_isWeekend() {
        // Jan 4 2025 (Saturday) 12:00 UTC
        assertEquals(DayType.WEEKEND, analyzer.getDayType(1735992000000L))
    }

    @Test
    fun sunday_isWeekend() {
        // Jan 5 2025 (Sunday) 12:00 UTC
        assertEquals(DayType.WEEKEND, analyzer.getDayType(1736078400000L))
    }

    @Test
    fun monday_isWeekday() {
        // Jan 6 2025 (Monday) 12:00 UTC
        assertEquals(DayType.WEEKDAY, analyzer.getDayType(1736164800000L))
    }

    @Test
    fun friday_isWeekday() {
        // Jan 10 2025 (Friday) 12:00 UTC
        assertEquals(DayType.WEEKDAY, analyzer.getDayType(1736510400000L))
    }

    // ── getTimeBin ────────────────────────────────────────────────────────────

    @Test
    fun hour0_isNight() {
        // Jan 6 2025 00:00 UTC
        assertEquals(TimeBin.NIGHT, analyzer.getTimeBin(1736121600000L))
    }

    @Test
    fun hour5_isNight() {
        // Jan 6 2025 05:00 UTC
        assertEquals(TimeBin.NIGHT, analyzer.getTimeBin(1736139600000L))
    }

    @Test
    fun hour6_isMorning_lowerBoundary() {
        // Jan 6 2025 06:00 UTC
        assertEquals(TimeBin.MORNING, analyzer.getTimeBin(1736143200000L))
    }

    @Test
    fun hour11_isMorning_upperBoundary() {
        // Jan 6 2025 11:00 UTC
        assertEquals(TimeBin.MORNING, analyzer.getTimeBin(1736161200000L))
    }

    @Test
    fun hour12_isDay_lowerBoundary() {
        // Jan 6 2025 12:00 UTC
        assertEquals(TimeBin.DAY, analyzer.getTimeBin(1736164800000L))
    }

    @Test
    fun hour17_isDay_upperBoundary() {
        // Jan 6 2025 17:00 UTC
        assertEquals(TimeBin.DAY, analyzer.getTimeBin(1736182800000L))
    }

    @Test
    fun hour18_isEvening_lowerBoundary() {
        // Jan 6 2025 18:00 UTC
        assertEquals(TimeBin.EVENING, analyzer.getTimeBin(1736186400000L))
    }

    @Test
    fun hour23_isEvening_upperBoundary() {
        // Jan 6 2025 23:00 UTC
        assertEquals(TimeBin.EVENING, analyzer.getTimeBin(1736204400000L))
    }

    // ── determineTransportType ────────────────────────────────────────────────

    @Test
    fun gpsLostAndHighSpeed_isMetro() {
        val result = analyzer.determineTransportType(
            averageSpeed = 20f / 3.6f,  // 20 km/h > 15 km/h threshold
            gpsLostDuration = 130_000L  // > 120s
        )
        assertEquals(TransportType.METRO, result)
    }

    @Test
    fun gpsLostButSpeedBelowMetroThreshold_isBus() {
        // GPS lost but speed only 12 km/h (< 15 km/h) → falls into BUS range
        val result = analyzer.determineTransportType(
            averageSpeed = 12f / 3.6f,
            gpsLostDuration = 130_000L
        )
        assertEquals(TransportType.BUS, result)
    }

    @Test
    fun gpsLostDurationExactlyAtThreshold_isNotMetro() {
        // Exactly 120_000L is not > 120_000L
        val result = analyzer.determineTransportType(
            averageSpeed = 20f / 3.6f,
            gpsLostDuration = 120_000L
        )
        assertEquals(TransportType.BUS, result)
    }

    @Test
    fun speedBelow7kmh_isWalk() {
        val result = analyzer.determineTransportType(
            averageSpeed = 5f / 3.6f,
            gpsLostDuration = 0L
        )
        assertEquals(TransportType.WALK, result)
    }

    @Test
    fun speedExactly7kmh_isBus_lowerBoundary() {
        val result = analyzer.determineTransportType(
            averageSpeed = 7f / 3.6f,
            gpsLostDuration = 0L
        )
        assertEquals(TransportType.BUS, result)
    }

    @Test
    fun speedExactly40kmh_isBus_upperBoundary() {
        val result = analyzer.determineTransportType(
            averageSpeed = 40f / 3.6f,
            gpsLostDuration = 0L
        )
        assertEquals(TransportType.BUS, result)
    }

    @Test
    fun speedAbove40kmh_isUnknown() {
        val result = analyzer.determineTransportType(
            averageSpeed = 50f / 3.6f,
            gpsLostDuration = 0L
        )
        assertEquals(TransportType.UNKNOWN, result)
    }
}
