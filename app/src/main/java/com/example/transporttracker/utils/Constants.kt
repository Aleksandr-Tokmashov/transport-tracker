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
}
