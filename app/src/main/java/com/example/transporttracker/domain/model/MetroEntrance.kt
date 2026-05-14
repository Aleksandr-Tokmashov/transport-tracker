package com.example.transporttracker.domain.model

data class MetroEntrance(
    val stationName: String,
    val line: String,
    val latitude: Double,
    val longitude: Double
)