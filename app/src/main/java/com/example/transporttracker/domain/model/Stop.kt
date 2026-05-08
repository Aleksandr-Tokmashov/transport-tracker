package com.example.transporttracker.domain.model

data class Stop(

    val stopId: Long,

    val stopName: String,

    val latitude: Double,

    val longitude: Double,

    val transportType: String
)