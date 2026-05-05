package com.example.transporttracker.domain.usecase

class TripDetector {

    companion object {

        const val START_SPEED = 7f

        const val STOP_SPEED = 3f

        const val MIN_TRIP_DURATION =
            60_000L

        const val STOP_DURATION =
            120_000L
    }

    private var isTripActive = false

    private var potentialStartTime = -1L

    private var lastMovingTime = 0L

    fun process(
        speedKmh: Float,
        timestamp: Long
    ): TripEvent? {

        // TRIP NOT ACTIVE YET

        if (!isTripActive) {

            if (speedKmh > START_SPEED) {

                if (potentialStartTime == -1L) {

                    potentialStartTime = timestamp
                }

                if (
                    timestamp - potentialStartTime >=
                    MIN_TRIP_DURATION
                ) {

                    isTripActive = true

                    lastMovingTime = timestamp

                    return TripEvent.TripStarted
                }

            } else {

                potentialStartTime = -1L
            }

            return null
        }

        // TRIP ACTIVE

        if (speedKmh > STOP_SPEED) {

            lastMovingTime = timestamp

            return null
        }

        // USER STOPPED

        if (
            timestamp - lastMovingTime >=
            STOP_DURATION
        ) {

            isTripActive = false

            potentialStartTime = -1L

            lastMovingTime = 0L

            return TripEvent.TripEnded
        }

        return null
    }

    fun isTripActive(): Boolean {
        return isTripActive
    }
}