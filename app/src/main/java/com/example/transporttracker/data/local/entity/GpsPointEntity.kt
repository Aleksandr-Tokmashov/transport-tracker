package com.example.transporttracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_points")
data class GpsPointEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp: Long,

    val latitude: Double,

    val longitude: Double,

    val speed: Float,

    val accuracy: Float,

    val tripId: Long? = null
)