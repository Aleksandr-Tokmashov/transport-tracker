package com.example.transporttracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patterns")
data class PatternEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val transportType: String,

    val dayType: String,

    val timeBin: String,

    val count: Int
)