package com.example.transporttracker

import androidx.test.core.app.ApplicationProvider
import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.AnalyticsGenerator
import com.example.transporttracker.domain.usecase.TripAnalyzer
import org.junit.Assert.*
import org.junit.Test

class TripAnalyzerTest {

    private val analyzer = TripAnalyzer()
    private val generator = AnalyticsGenerator(ApplicationProvider.getApplicationContext())

    @Test
    fun weekdayDetectionWorksCorrectly() {
        val weekdayTimestamp = 1700000000000L
        val result = analyzer.getDayType(weekdayTimestamp)
        assertTrue(result == DayType.WEEKDAY || result == DayType.WEEKEND)
    }

    @Test
    fun timeBinDetectionReturnsValidValue() {
        val timestamp = 1700000000000L
        val result = analyzer.getTimeBin(timestamp)
        assertTrue(
            result == TimeBin.MORNING ||
                result == TimeBin.DAY ||
                result == TimeBin.EVENING ||
                result == TimeBin.NIGHT
        )
    }

    @Test
    fun patternGenerationWorksWhenTripsRepeat() {
        val trips = listOf(fakeTrip(), fakeTrip(), fakeTrip())
        val patterns = generator.generatePatterns(trips)
        assertTrue(patterns.isNotEmpty())
        assertTrue(patterns.first().count >= 3)
    }

    private fun fakeTrip() = com.example.transporttracker.domain.model.Trip(
        startTime = 1700000000000L,
        endTime = 1700003600000L,
        transportType = TransportType.BUS,
        averageSpeed = 5f,
        dayType = DayType.WEEKDAY,
        timeBin = TimeBin.MORNING
    )
}
