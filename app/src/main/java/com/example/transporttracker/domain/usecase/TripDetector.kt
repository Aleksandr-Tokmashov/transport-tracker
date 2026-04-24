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

        // MOVING

        if (speedKmh > START_SPEED) {

            if (!isTripActive) {

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

                lastMovingTime = timestamp
            }
        }

        // NOT MOVING

        else {

            if (!isTripActive) {

                potentialStartTime = -1L
            }

            if (
                isTripActive &&
                speedKmh < STOP_SPEED
            ) {

                if (
                    timestamp - lastMovingTime >=
                    STOP_DURATION
                ) {

                    isTripActive = false

                    potentialStartTime = -1L

                    lastMovingTime = 0L

                    return TripEvent.TripEnded
                }
            }
        }

        return null
    }

    fun isTripActive(): Boolean {
        return isTripActive
    }
}