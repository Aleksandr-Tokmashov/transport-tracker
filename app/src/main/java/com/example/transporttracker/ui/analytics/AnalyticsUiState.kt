package com.example.transporttracker.ui.analytics

data class AnalyticsUiState(

    val insights: List<String> = emptyList(),

    val totalTrips: Int = 0,

    val mostUsedTransport: String = "UNKNOWN"
)