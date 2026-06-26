package com.example.transporttracker

import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.data.local.entity.TripSegmentEntity
import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.utils.TripMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripMapperTest {

    private fun entity(
        id: Long = 1L,
        startTime: Long = 1_000L,
        endTime: Long = 2_000L,
        transportType: String = "BUS",
        averageSpeed: Float = 5f,
        dayType: String = "WEEKDAY",
        timeBin: String = "MORNING",
        distanceMeters: Float = 1_000f
    ) = TripEntity(id, startTime, endTime, transportType, averageSpeed, dayType, timeBin, distanceMeters)

    private fun segEntity(
        id: Long = 1L,
        tripId: Long = 1L,
        startTime: Long = 1_000L,
        endTime: Long = 1_500L,
        transportType: String = "METRO",
        averageSpeed: Float = 12f
    ) = TripSegmentEntity(id, tripId, startTime, endTime, transportType, averageSpeed)

    @Test
    fun mapsAllFieldsFromEntity() {
        val trip = TripMapper.map(entity())
        assertEquals(1L, trip.id)
        assertEquals(1_000L, trip.startTime)
        assertEquals(2_000L, trip.endTime)
        assertEquals(TransportType.BUS, trip.transportType)
        assertEquals(5f, trip.averageSpeed)
        assertEquals(DayType.WEEKDAY, trip.dayType)
        assertEquals(TimeBin.MORNING, trip.timeBin)
        assertEquals(1_000f, trip.distanceMeters)
        assertTrue(trip.segments.isEmpty())
    }

    @Test
    fun invalidTransportType_fallsBackToUnknown() {
        val trip = TripMapper.map(entity(transportType = "HOVERCRAFT"))
        assertEquals(TransportType.UNKNOWN, trip.transportType)
    }

    @Test
    fun invalidDayType_fallsBackToWeekday() {
        val trip = TripMapper.map(entity(dayType = "HOLIDAY"))
        assertEquals(DayType.WEEKDAY, trip.dayType)
    }

    @Test
    fun invalidTimeBin_fallsBackToDay() {
        val trip = TripMapper.map(entity(timeBin = "AFTERNOON"))
        assertEquals(TimeBin.DAY, trip.timeBin)
    }

    @Test
    fun emptyTransportType_fallsBackToUnknown() {
        val trip = TripMapper.map(entity(transportType = ""))
        assertEquals(TransportType.UNKNOWN, trip.transportType)
    }

    @Test
    fun allValidTransportTypes_mappedCorrectly() {
        TransportType.entries.forEach { type ->
            val trip = TripMapper.map(entity(transportType = type.name))
            assertEquals(type, trip.transportType)
        }
    }

    @Test
    fun allValidDayTypes_mappedCorrectly() {
        DayType.entries.forEach { type ->
            val trip = TripMapper.map(entity(dayType = type.name))
            assertEquals(type, trip.dayType)
        }
    }

    @Test
    fun allValidTimeBins_mappedCorrectly() {
        TimeBin.entries.forEach { type ->
            val trip = TripMapper.map(entity(timeBin = type.name))
            assertEquals(type, trip.timeBin)
        }
    }

    @Test
    fun segmentsAreMappedInOrder() {
        val trip = TripMapper.map(
            entity(),
            listOf(
                segEntity(id = 1L, transportType = "METRO"),
                segEntity(id = 2L, transportType = "WALK")
            )
        )
        assertEquals(2, trip.segments.size)
        assertEquals(TransportType.METRO, trip.segments[0].transportType)
        assertEquals(TransportType.WALK, trip.segments[1].transportType)
    }

    @Test
    fun segmentWithInvalidTransportType_fallsBackToUnknown() {
        val trip = TripMapper.map(entity(), listOf(segEntity(transportType = "TELEPORT")))
        assertEquals(TransportType.UNKNOWN, trip.segments[0].transportType)
    }

    @Test
    fun segmentFieldsAreCopied() {
        val seg = segEntity(id = 42L, tripId = 1L, startTime = 100L, endTime = 200L, averageSpeed = 7f)
        val trip = TripMapper.map(entity(), listOf(seg))
        val mapped = trip.segments[0]
        assertEquals(42L, mapped.id)
        assertEquals(1L, mapped.tripId)
        assertEquals(100L, mapped.startTime)
        assertEquals(200L, mapped.endTime)
        assertEquals(7f, mapped.averageSpeed)
    }

    @Test
    fun noSegmentsArgument_defaultsToEmptyList() {
        val trip = TripMapper.map(entity())
        assertTrue(trip.segments.isEmpty())
    }
}
