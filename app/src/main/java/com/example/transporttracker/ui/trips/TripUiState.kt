package com.example.transporttracker.ui.trips

import com.example.transporttracker.domain.model.TransportType

data class TripUiState(
    val id: Long,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    val transportType: String,
    val averageSpeed: String,
    val segments: List<String> = emptyList(),
    val transportTypeEnum: TransportType = TransportType.UNKNOWN,
    val segmentTypes: List<TransportType> = emptyList(),
    val distance: String = "",
    // non-zero when this card represents a single segment of a multi-leg trip
    val segmentStartTime: Long = 0L,
    val segmentEndTime: Long = 0L
)
