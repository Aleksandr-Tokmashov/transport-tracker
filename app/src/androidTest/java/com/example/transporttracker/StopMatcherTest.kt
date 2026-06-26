package com.example.transporttracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.domain.model.Stop
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.usecase.StopMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StopMatcherTest {

    private lateinit var matcher: StopMatcher

    private val refLat = 55.7647
    private val refLon = 37.6059

    // 0.0009 lat ≈ 100m; keep inside the 100m hard radius
    private val stopAt50m  = Stop(1, "Близко",    refLat + 0.00045, refLon, "Автобус")
    private val stopAt80m  = Stop(2, "Рядом",     refLat + 0.00072, refLon, "Трамвай")
    // 0.00110 lat ≈ 122m — just outside the 100m radius
    private val stopAt120m = Stop(3, "Далеко",    refLat + 0.00108, refLon, "Автобус")

    @Before
    fun setup() {
        matcher = StopMatcher()
    }

    // ── detectTransportType ───────────────────────────────────────────────────

    @Test
    fun nullStop_returnsUnknown() {
        assertEquals(TransportType.UNKNOWN, matcher.detectTransportType(null))
    }

    @Test
    fun busStop_returnsBus() {
        val stop = Stop(1, "Тест", refLat, refLon, "Автобус")
        assertEquals(TransportType.BUS, matcher.detectTransportType(stop))
    }

    @Test
    fun busStop_caseInsensitive() {
        val stop = Stop(1, "Тест", refLat, refLon, "АВТОБУС")
        assertEquals(TransportType.BUS, matcher.detectTransportType(stop))
    }

    @Test
    fun tramStop_returnsTram() {
        val stop = Stop(1, "Тест", refLat, refLon, "Трамвай")
        assertEquals(TransportType.TRAM, matcher.detectTransportType(stop))
    }

    @Test
    fun tramStop_caseInsensitive() {
        val stop = Stop(1, "Тест", refLat, refLon, "ТРАМВАЙ")
        assertEquals(TransportType.TRAM, matcher.detectTransportType(stop))
    }

    @Test
    fun unknownTransportString_returnsUnknown() {
        val stop = Stop(1, "Тест", refLat, refLon, "Монорельс")
        assertEquals(TransportType.UNKNOWN, matcher.detectTransportType(stop))
    }

    @Test
    fun emptyTransportString_returnsUnknown() {
        val stop = Stop(1, "Тест", refLat, refLon, "")
        assertEquals(TransportType.UNKNOWN, matcher.detectTransportType(stop))
    }

    // ── findNearestStop ───────────────────────────────────────────────────────

    @Test
    fun emptyList_returnsNull() {
        assertNull(matcher.findNearestStop(refLat, refLon, emptyList()))
    }

    @Test
    fun stopWithin100m_isReturned() {
        assertNotNull(matcher.findNearestStop(refLat, refLon, listOf(stopAt50m)))
    }

    @Test
    fun stopAt80m_isWithinRadius() {
        assertNotNull(matcher.findNearestStop(refLat, refLon, listOf(stopAt80m)))
    }

    @Test
    fun stopBeyond100m_returnsNull() {
        assertNull(matcher.findNearestStop(refLat, refLon, listOf(stopAt120m)))
    }

    @Test
    fun multipleStops_returnsNearestWithinRadius() {
        val result = matcher.findNearestStop(refLat, refLon, listOf(stopAt80m, stopAt50m, stopAt120m))
        assertEquals("Близко", result?.stopName)
    }

    @Test
    fun allStopsOutsideRadius_returnsNull() {
        assertNull(matcher.findNearestStop(refLat, refLon, listOf(stopAt120m)))
    }
}
