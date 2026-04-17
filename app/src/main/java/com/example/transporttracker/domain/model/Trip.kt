package com.example.transporttracker.domain.model

data class Trip(

    val id: Long = 0L,

    val startTime: Long,

    val endTime: Long,

    val transportType: TransportType,

    val averageSpeed: Float,

    val dayType: DayType,

    val timeBin: TimeBin
)