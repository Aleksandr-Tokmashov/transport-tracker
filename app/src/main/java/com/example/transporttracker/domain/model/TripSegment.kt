package com.example.transporttracker.domain.model

data class TripSegment(
    val id: Long = 0L,
    val tripId: Long,
    val startTime: Long,
    val endTime: Long,
    val transportType: TransportType,
    val averageSpeed: Float
)
