package com.example.transporttracker.domain.usecase

class WalkDetector {

    fun isWalking(
        speedMps: Float,
        nearStop: Boolean,
        nearMetro: Boolean,
        nearMcd: Boolean
    ): Boolean {

        val speedKmh =
            speedMps * 3.6f

        return (
                speedKmh in 1f..7f &&
                        !nearMetro &&
                        !nearMcd &&
                        !nearStop
                )
    }
}