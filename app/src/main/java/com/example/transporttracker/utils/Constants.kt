package com.example.transporttracker.utils

object Constants {

    const val LOCATION_INTERVAL = 5000L

    const val LOCATION_FASTEST_INTERVAL = 3000L

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"

    const val NOTIFICATION_ID = 1

    // Speed below which a stop counts as a potential transfer (~5 km/h)
    const val TRANSFER_SPEED_MPS = 1.4f

    // How long the user must be slow before the current segment is sealed
    const val TRANSFER_PAUSE_DURATION = 20_000L

    // How long GPS must be degraded during a trip to vote for metro (underground)
    const val GPS_LOSS_METRO_DURATION = 30_000L

    // Trips older than this at service restore time are considered abandoned and deleted
    const val MAX_TRIP_DURATION_MS = 4 * 60 * 60 * 1000L
}
