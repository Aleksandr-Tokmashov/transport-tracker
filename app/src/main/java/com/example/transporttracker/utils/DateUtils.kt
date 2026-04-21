package com.example.transporttracker.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val formatter =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        )

    fun formatTime(
        timestamp: Long
    ): String {

        return formatter.format(
            Date(timestamp)
        )
    }
}