package com.example.transporttracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_segments")
data class TripSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val startTime: Long,
    val endTime: Long,
    val transportType: String,
    val averageSpeed: Float
)
