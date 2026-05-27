package com.example.transporttracker.ui.trips

data class TripUiState(

    val id: Long,

    val date: String,

    val startTime: String,

    val endTime: String,

    val duration: String,

    val transportType: String,

    val averageSpeed: String,

    val segments: List<String> = emptyList()
)
