package com.example.transporttracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.domain.model.McdEntrance
import com.example.transporttracker.domain.usecase.McdMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class McdMatcherTest {

    private lateinit var matcher: McdMatcher

    private val refLat = 55.7647
    private val refLon = 37.6059

    private val stationAtRef    = McdEntrance("Беговая", "МЦД-1", refLat, refLon)
    private val station150m     = McdEntrance("Тестовская", "МЦД-1", refLat + 0.00135, refLon)
    private val station500m     = McdEntrance("Дальняя", "МЦД-2", refLat + 0.0045, refLon)

    @Before
    fun setup() {
        matcher = McdMatcher()
    }

    @Test
    fun emptyList_findNearest_returnsNull() {
        assertNull(matcher.findNearestMcd(refLat, refLon, emptyList()))
    }

    @Test
    fun singleStation_returnsIt() {
        assertEquals(stationAtRef, matcher.findNearestMcd(refLat, refLon, listOf(stationAtRef)))
    }

    @Test
    fun multipleStations_returnsNearest() {
        val result = matcher.findNearestMcd(refLat, refLon, listOf(station500m, station150m, stationAtRef))
        assertEquals(stationAtRef, result)
    }

    @Test
    fun emptyList_isNearMcd_returnsNull() {
        assertNull(matcher.isNearMcd(refLat, refLon, emptyList()))
    }

    @Test
    fun stationAtSamePoint_isWithinRadius() {
        assertNotNull(matcher.isNearMcd(refLat, refLon, listOf(stationAtRef)))
    }

    @Test
    fun station150m_withinDefaultRadius() {
        assertNotNull(matcher.isNearMcd(refLat, refLon, listOf(station150m)))
    }

    @Test
    fun station500m_outsideDefaultRadius() {
        assertNull(matcher.isNearMcd(refLat, refLon, listOf(station500m)))
    }

    @Test
    fun customRadius600_includes500mStation() {
        assertNotNull(matcher.isNearMcd(refLat, refLon, listOf(station500m), radiusMeters = 600f))
    }

    @Test
    fun customRadius100_excludes150mStation() {
        assertNull(matcher.isNearMcd(refLat, refLon, listOf(station150m), radiusMeters = 100f))
    }
}
