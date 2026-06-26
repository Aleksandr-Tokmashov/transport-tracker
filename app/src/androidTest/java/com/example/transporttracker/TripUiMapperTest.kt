package com.example.transporttracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.domain.model.TripSegment
import com.example.transporttracker.utils.TripFormatter
import com.example.transporttracker.utils.TripUiMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TripUiMapperTest {

    private lateinit var mapper: TripUiMapper

    private val baseTrip = Trip(
        id = 1L,
        startTime = 1_000L,
        endTime = 10_000L,
        transportType = TransportType.BUS,
        averageSpeed = 5f,
        dayType = DayType.WEEKDAY,
        timeBin = TimeBin.MORNING,
        distanceMeters = 1_000f
    )

    private fun segment(
        id: Long,
        startTime: Long,
        endTime: Long,
        type: TransportType
    ) = TripSegment(id, 1L, startTime, endTime, type, 5f)

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        mapper = TripUiMapper(ctx, TripFormatter(ctx))
    }

    // ── mapToCards: single-card cases ─────────────────────────────────────────

    @Test
    fun noSegments_returnsSingleCard() {
        val cards = mapper.mapToCards(baseTrip)
        assertEquals(1, cards.size)
    }

    @Test
    fun oneSegment_returnsSingleCard() {
        val trip = baseTrip.copy(segments = listOf(segment(1, 1_000L, 10_000L, TransportType.METRO)))
        val cards = mapper.mapToCards(trip)
        assertEquals(1, cards.size)
    }

    @Test
    fun twoWalkSegments_returnsSingleCard() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L, 5_000L, TransportType.WALK),
                segment(2, 5_000L, 10_000L, TransportType.WALK)
            )
        )
        val cards = mapper.mapToCards(trip)
        assertEquals(1, cards.size)
    }

    @Test
    fun oneMeaningfulAndOneWalk_returnsSingleCard() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L, 7_000L, TransportType.METRO),
                segment(2, 7_000L, 10_000L, TransportType.WALK)
            )
        )
        // Only 1 non-walk segment → falls back to single card
        val cards = mapper.mapToCards(trip)
        assertEquals(1, cards.size)
    }

    @Test
    fun oneUnknownAndOneMeaningful_returnsSingleCard() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L, 5_000L, TransportType.UNKNOWN),
                segment(2, 5_000L, 10_000L, TransportType.BUS)
            )
        )
        assertEquals(1, mapper.mapToCards(trip).size)
    }

    // ── mapToCards: multi-card cases ──────────────────────────────────────────

    @Test
    fun tramAndMetroSegments_returnsTwoCards() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L,  5_000L, TransportType.TRAM),
                segment(2, 5_000L, 10_000L, TransportType.METRO)
            )
        )
        val cards = mapper.mapToCards(trip)
        assertEquals(2, cards.size)
    }

    @Test
    fun multiCard_eachHasCorrectTransportType() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L,  5_000L, TransportType.TRAM),
                segment(2, 5_000L, 10_000L, TransportType.METRO)
            )
        )
        val cards = mapper.mapToCards(trip)
        assertEquals(TransportType.TRAM,  cards[0].transportTypeEnum)
        assertEquals(TransportType.METRO, cards[1].transportTypeEnum)
    }

    @Test
    fun multiCard_eachHasCorrectSegmentTimeBounds() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L,  5_000L, TransportType.TRAM),
                segment(2, 5_000L, 10_000L, TransportType.METRO)
            )
        )
        val cards = mapper.mapToCards(trip)
        assertEquals(1_000L,  cards[0].segmentStartTime)
        assertEquals(5_000L,  cards[0].segmentEndTime)
        assertEquals(5_000L,  cards[1].segmentStartTime)
        assertEquals(10_000L, cards[1].segmentEndTime)
    }

    @Test
    fun multiCard_allShareSameTripId() {
        val trip = baseTrip.copy(
            id = 42L,
            segments = listOf(
                segment(1, 1_000L, 5_000L, TransportType.BUS),
                segment(2, 5_000L, 10_000L, TransportType.METRO)
            )
        )
        val cards = mapper.mapToCards(trip)
        assertTrue(cards.all { it.id == 42L })
    }

    @Test
    fun walkSegmentBetweenTwoMeaningful_isExcludedFromCards() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L,  4_000L, TransportType.TRAM),
                segment(2, 4_000L,  6_000L, TransportType.WALK),
                segment(3, 6_000L, 10_000L, TransportType.METRO)
            )
        )
        val cards = mapper.mapToCards(trip)
        assertEquals(2, cards.size)
        assertEquals(TransportType.TRAM,  cards[0].transportTypeEnum)
        assertEquals(TransportType.METRO, cards[1].transportTypeEnum)
    }

    @Test
    fun threeSegments_returnsThreeCards() {
        val trip = baseTrip.copy(
            segments = listOf(
                segment(1, 1_000L,  4_000L, TransportType.TRAM),
                segment(2, 4_000L,  7_000L, TransportType.METRO),
                segment(3, 7_000L, 10_000L, TransportType.BUS)
            )
        )
        assertEquals(3, mapper.mapToCards(trip).size)
    }

    // ── single-card path: correct fields ─────────────────────────────────────

    @Test
    fun singleCard_hasZeroSegmentTimeBounds() {
        val cards = mapper.mapToCards(baseTrip)
        assertEquals(0L, cards[0].segmentStartTime)
        assertEquals(0L, cards[0].segmentEndTime)
    }

    @Test
    fun singleCard_usesTripsTransportType() {
        val cards = mapper.mapToCards(baseTrip)
        assertEquals(baseTrip.transportType, cards[0].transportTypeEnum)
    }
}
