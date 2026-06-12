package com.example.transporttracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val transportType: String,
    val averageSpeed: Float,
    val dayType: String,
    val timeBin: String,
    val distanceMeters: Float = 0f
)
