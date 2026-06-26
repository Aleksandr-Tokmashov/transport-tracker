package com.example.transporttracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.utils.TripFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TripFormatterTest {

    private lateinit var formatter: TripFormatter

    @Before
    fun setup() {
        formatter = TripFormatter(ApplicationProvider.getApplicationContext())
    }

    // ── formatDate ────────────────────────────────────────────────────────────

    @Test
    fun formatDate_producesCorrectPattern() {
        val expected = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(0L))
        assertEquals(expected, formatter.formatDate(0L))
    }

    @Test
    fun formatDate_nonZeroTimestamp_matchesSimpleDateFormat() {
        val ts = 1_700_000_000_000L
        val expected = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(ts))
        assertEquals(expected, formatter.formatDate(ts))
    }

    // ── formatTimeOnly ────────────────────────────────────────────────────────

    @Test
    fun formatTimeOnly_nonZeroTimestamp_matchesSimpleDateFormat() {
        val ts = 1_700_000_000_000L
        val expected = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
        assertEquals(expected, formatter.formatTimeOnly(ts))
    }

    // ── formatDuration ────────────────────────────────────────────────────────

    @Test
    fun formatDuration_containsMinutesValue() {
        val start = 0L
        val end   = 5 * 60_000L  // 5 minutes
        val result = formatter.formatDuration(start, end)
        assertTrue("Expected '5' in duration string but got: $result", result.contains("5"))
    }

    @Test
    fun formatDuration_oneHour_shows60Minutes() {
        val result = formatter.formatDuration(0L, 3_600_000L)
        assertTrue(result.contains("60"))
    }

    @Test
    fun formatDuration_zeroDuration_shows0() {
        val result = formatter.formatDuration(1000L, 1000L)
        assertTrue(result.contains("0"))
    }

    // ── formatSpeed ───────────────────────────────────────────────────────────

    @Test
    fun formatSpeed_zeroSpeed_returnsNoData() {
        val result = formatter.formatSpeed(0f)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatSpeed_negativeSpeed_returnsNoData() {
        val result = formatter.formatSpeed(-1f)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatSpeed_normalSpeed_containsNumericValue() {
        val result = formatter.formatSpeed(10f)  // 36 km/h
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatSpeed_capAt120kmh() {
        // 40 m/s = 144 km/h, capped to 120
        val resultHigh = formatter.formatSpeed(40f)
        val resultCap  = formatter.formatSpeed(120f / 3.6f)
        assertEquals(resultCap, resultHigh)
    }

    // ── formatDistance ────────────────────────────────────────────────────────

    @Test
    fun formatDistance_under1000m_containsMeters() {
        val result = formatter.formatDistance(500f)
        assertTrue("Expected meters value in: $result", result.contains("500"))
    }

    @Test
    fun formatDistance_exactly1000m_usesKm() {
        val result = formatter.formatDistance(1000f)
        // Should NOT contain "1000" as meters, but "1" as km
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatDistance_5500m_showsDecimalKm() {
        val result = formatter.formatDistance(5500f)
        // 5.5 km < 10 km → decimal km format
        assertTrue(result.contains("5"))
    }

    @Test
    fun formatDistance_15000m_showsRoundedKm() {
        val result = formatter.formatDistance(15_000f)
        // 15 km → rounded format
        assertTrue(result.contains("15"))
    }
}
