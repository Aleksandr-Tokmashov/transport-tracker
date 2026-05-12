package com.example.transporttracker.ui.analytics

data class AnalyticsUiState(

    val totalTrips: Int = 0,

    val mostUsedTransport: String = "",

    val insights: List<AnalyticsInsight> = emptyList()
)

data class AnalyticsInsight(

    val text: String,

    val count: Int
)