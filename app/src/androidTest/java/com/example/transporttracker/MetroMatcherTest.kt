package com.example.transporttracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.domain.model.MetroEntrance
import com.example.transporttracker.domain.usecase.MetroMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetroMatcherTest {

    private lateinit var matcher: MetroMatcher

    // Reference point: Pushkin Square, Moscow (~55.7647, 37.6059)
    private val refLat = 55.7647
    private val refLon = 37.6059

    // Station placed exactly at the reference point
    private val stationAtRef = MetroEntrance("Пушкинская", "Кольцевая", refLat, refLon)

    // Station ~150m north of reference (1 lat degree ≈ 111 000 m → 0.00135 ≈ 150m)
    private val station150m = MetroEntrance("Тверская", "Замоскворецкая", refLat + 0.00135, refLon)

    // Station ~500m north of reference
    private val station500m = MetroEntrance("Маяковская", "Замоскворецкая", refLat + 0.0045, refLon)

    @Before
    fun setup() {
        matcher = MetroMatcher()
    }

    // ── findNearestMetro ──────────────────────────────────────────────────────

    @Test
    fun emptyList_returnsNull() {
        assertNull(matcher.findNearestMetro(refLat, refLon, emptyList()))
    }

    @Test
    fun singleStation_returned() {
        val result = matcher.findNearestMetro(refLat, refLon, listOf(stationAtRef))
        assertEquals(stationAtRef, result)
    }

    @Test
    fun multipleStations_returnsNearest() {
        val result = matcher.findNearestMetro(refLat, refLon, listOf(station500m, station150m, stationAtRef))
        assertEquals(stationAtRef, result)
    }

    // ── isNearMetro (default radius 300m) ────────────────────────────────────

    @Test
    fun emptyList_isNearMetro_returnsNull() {
        assertNull(matcher.isNearMetro(refLat, refLon, emptyList()))
    }

    @Test
    fun stationAtSamePoint_isWithinRadius() {
        assertNotNull(matcher.isNearMetro(refLat, refLon, listOf(stationAtRef)))
    }

    @Test
    fun stationAt150m_isWithinDefaultRadius() {
        assertNotNull(matcher.isNearMetro(refLat, refLon, listOf(station150m)))
    }

    @Test
    fun stationAt500m_isOutsideDefaultRadius() {
        assertNull(matcher.isNearMetro(refLat, refLon, listOf(station500m)))
    }

    @Test
    fun customRadius600m_includes500mStation() {
        assertNotNull(matcher.isNearMetro(refLat, refLon, listOf(station500m), radiusMeters = 600f))
    }

    @Test
    fun customRadius100m_excludes150mStation() {
        assertNull(matcher.isNearMetro(refLat, refLon, listOf(station150m), radiusMeters = 100f))
    }

    @Test
    fun returnsCorrectStationObject() {
        val result = matcher.isNearMetro(refLat, refLon, listOf(stationAtRef))
        assertEquals("Пушкинская", result?.stationName)
        assertEquals("Кольцевая", result?.line)
    }
}
