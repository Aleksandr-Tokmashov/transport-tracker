package com.example.transporttracker

import com.example.transporttracker.domain.usecase.WalkDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WalkDetectorTest {

    private lateinit var detector: WalkDetector

    @Before
    fun setup() {
        detector = WalkDetector()
    }

    @Test
    fun typicalWalkingSpeedNotNearAnything_returnsTrue() {
        assertTrue(
            detector.isWalking(
                speedMps = 1.5f,   // 5.4 km/h
                nearStop = false,
                nearMetro = false,
                nearMcd = false
            )
        )
    }

    @Test
    fun slowWalkingSpeed_returnsTrue() {
        assertTrue(
            detector.isWalking(
                speedMps = 0.5f,   // 1.8 km/h — minimum threshold
                nearStop = false,
                nearMetro = false,
                nearMcd = false
            )
        )
    }

    @Test
    fun nearMetro_returnsFalse() {
        assertFalse(
            detector.isWalking(
                speedMps = 1.5f,
                nearStop = false,
                nearMetro = true,
                nearMcd = false
            )
        )
    }

    @Test
    fun nearMcd_returnsFalse() {
        assertFalse(
            detector.isWalking(
                speedMps = 1.5f,
                nearStop = false,
                nearMetro = false,
                nearMcd = true
            )
        )
    }

    @Test
    fun nearBusStop_returnsFalse() {
        assertFalse(
            detector.isWalking(
                speedMps = 1.5f,
                nearStop = true,
                nearMetro = false,
                nearMcd = false
            )
        )
    }

    @Test
    fun speedTooHighForWalking_returnsFalse() {
        assertFalse(
            detector.isWalking(
                speedMps = 2.5f,   // 9 km/h — above walking range
                nearStop = false,
                nearMetro = false,
                nearMcd = false
            )
        )
    }

    @Test
    fun speedTooLow_returnsFalse() {
        assertFalse(
            detector.isWalking(
                speedMps = 0.1f,   // 0.36 km/h — standing still
                nearStop = false,
                nearMetro = false,
                nearMcd = false
            )
        )
    }
}
