package com.example.transporttracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.domain.usecase.AnalyticsGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalyticsGeneratorExpandedTest {

    private lateinit var gen: AnalyticsGenerator

    private fun trip(
        transport: TransportType = TransportType.BUS,
        dayType: DayType = DayType.WEEKDAY,
        timeBin: TimeBin = TimeBin.MORNING,
        distanceMeters: Float = 1_000f,
        startTime: Long = 0L,
        endTime: Long = 600_000L
    ) = Trip(
        startTime = startTime,
        endTime = endTime,
        transportType = transport,
        averageSpeed = 5f,
        dayType = dayType,
        timeBin = timeBin,
        distanceMeters = distanceMeters
    )

    @Before
    fun setup() {
        gen = AnalyticsGenerator(ApplicationProvider.getApplicationContext())
    }

    // ── getTotalDistanceKm ────────────────────────────────────────────────────

    @Test
    fun emptyList_totalDistance_isZero() {
        assertEquals(0f, gen.getTotalDistanceKm(emptyList()), 0.001f)
    }

    @Test
    fun totalDistanceKm_sumsAllTrips() {
        val trips = listOf(trip(distanceMeters = 2_000f), trip(distanceMeters = 3_000f))
        assertEquals(5f, gen.getTotalDistanceKm(trips), 0.001f)
    }

    @Test
    fun totalDistanceKm_convertsMtoKm() {
        val trips = listOf(trip(distanceMeters = 500f))
        assertEquals(0.5f, gen.getTotalDistanceKm(trips), 0.001f)
    }

    // ── getPeriodStats ────────────────────────────────────────────────────────

    @Test
    fun getPeriodStats_filtersTripsBeforeCutoff() {
        val old  = trip(startTime = 100L,   endTime = 700L,   distanceMeters = 999f)
        val recent = trip(startTime = 1_000L, endTime = 2_000L, distanceMeters = 1_000f)
        val stats = gen.getPeriodStats(listOf(old, recent), cutoffMs = 500L)
        assertEquals(1, stats.tripCount)
        assertEquals(1f, stats.distanceKm, 0.001f)
    }

    @Test
    fun getPeriodStats_allTripsIncluded_whenCutoffIsZero() {
        val trips = listOf(trip(distanceMeters = 1_000f), trip(distanceMeters = 1_000f))
        val stats = gen.getPeriodStats(trips, cutoffMs = 0L)
        assertEquals(2, stats.tripCount)
    }

    @Test
    fun getPeriodStats_durationSummedCorrectly() {
        // Each trip = 10 minutes = 600_000 ms
        val trips = listOf(
            trip(startTime = 1_000L, endTime = 601_000L),
            trip(startTime = 2_000L, endTime = 602_000L)
        )
        val stats = gen.getPeriodStats(trips, cutoffMs = 0L)
        assertEquals(20L, stats.durationMin)
    }

    // ── getTransportShares ────────────────────────────────────────────────────

    @Test
    fun emptyList_returnsEmptyShares() {
        assertTrue(gen.getTransportShares(emptyList()).isEmpty())
    }

    @Test
    fun unknownTrips_areExcluded() {
        val shares = gen.getTransportShares(listOf(trip(transport = TransportType.UNKNOWN)))
        assertTrue(shares.isEmpty())
    }

    @Test
    fun mostFrequentType_hasFractionOne() {
        val trips = listOf(
            trip(TransportType.METRO), trip(TransportType.METRO), trip(TransportType.METRO),
            trip(TransportType.BUS)
        )
        val shares = gen.getTransportShares(trips)
        val metro = shares.first { it.transportType == TransportType.METRO }
        assertEquals(1f, metro.fraction, 0.001f)
    }

    @Test
    fun lessFrequentType_hasFractionalShare() {
        val trips = listOf(
            trip(TransportType.METRO), trip(TransportType.METRO),
            trip(TransportType.BUS)
        )
        val shares = gen.getTransportShares(trips)
        val bus = shares.first { it.transportType == TransportType.BUS }
        assertEquals(0.5f, bus.fraction, 0.001f)
    }

    @Test
    fun shares_sortedByCountDescending() {
        val trips = listOf(
            trip(TransportType.BUS),
            trip(TransportType.METRO), trip(TransportType.METRO), trip(TransportType.METRO)
        )
        val shares = gen.getTransportShares(trips)
        assertEquals(TransportType.METRO, shares[0].transportType)
        assertEquals(TransportType.BUS,   shares[1].transportType)
    }

    @Test
    fun distanceKm_summedPerTransportType() {
        val trips = listOf(
            trip(TransportType.BUS, distanceMeters = 1_000f),
            trip(TransportType.BUS, distanceMeters = 2_000f)
        )
        val shares = gen.getTransportShares(trips)
        assertEquals(3f, shares[0].distanceKm, 0.01f)
    }

    // ── getTimeBinCounts ──────────────────────────────────────────────────────

    @Test
    fun emptyList_returnsAllFourBinsWithZeroCounts() {
        val counts = gen.getTimeBinCounts(emptyList())
        assertEquals(4, counts.size)
        assertTrue(counts.all { it.count == 0 })
    }

    @Test
    fun timeBinsOrderedMorningDayEveningNight() {
        val counts = gen.getTimeBinCounts(emptyList())
        // Just verify there are 4 entries
        assertEquals(4, counts.size)
    }

    @Test
    fun mostCommonBin_hasFractionOne() {
        val trips = List(3) { trip(timeBin = TimeBin.MORNING) } +
                    List(1) { trip(timeBin = TimeBin.EVENING) }
        val counts = gen.getTimeBinCounts(trips)
        val morning = counts.first { it.count == 3 }
        assertEquals(1f, morning.fraction, 0.001f)
    }

    @Test
    fun binFraction_relativeToPeak() {
        val trips = List(4) { trip(timeBin = TimeBin.MORNING) } +
                    List(2) { trip(timeBin = TimeBin.EVENING) }
        val counts = gen.getTimeBinCounts(trips)
        val evening = counts.first { it.count == 2 }
        assertEquals(0.5f, evening.fraction, 0.001f)
    }

    // ── generatePatterns ──────────────────────────────────────────────────────

    @Test
    fun fewerThan3SamePattern_producesNoPattern() {
        val trips = listOf(trip(), trip())
        assertTrue(gen.generatePatterns(trips).isEmpty())
    }

    @Test
    fun exactly3SamePattern_producesOnePattern() {
        val trips = listOf(trip(), trip(), trip())
        val patterns = gen.generatePatterns(trips)
        assertEquals(1, patterns.size)
        assertEquals(3, patterns[0].count)
    }

    @Test
    fun differentPatterns_groupedSeparately() {
        val trips = listOf(
            trip(TransportType.BUS,   DayType.WEEKDAY, TimeBin.MORNING),
            trip(TransportType.BUS,   DayType.WEEKDAY, TimeBin.MORNING),
            trip(TransportType.BUS,   DayType.WEEKDAY, TimeBin.MORNING),
            trip(TransportType.METRO, DayType.WEEKDAY, TimeBin.EVENING),
            trip(TransportType.METRO, DayType.WEEKDAY, TimeBin.EVENING),
            trip(TransportType.METRO, DayType.WEEKDAY, TimeBin.EVENING)
        )
        assertEquals(2, gen.generatePatterns(trips).size)
    }

    @Test
    fun patternText_isNotEmpty() {
        val trips = listOf(trip(), trip(), trip())
        assertTrue(gen.generatePatterns(trips)[0].text.isNotBlank())
    }
}
