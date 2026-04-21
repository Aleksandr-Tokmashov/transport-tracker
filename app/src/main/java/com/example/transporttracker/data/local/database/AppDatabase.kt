package com.example.transporttracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.PatternDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.PatternEntity
import com.example.transporttracker.data.local.entity.TripEntity

@Database(
    entities = [
        GpsPointEntity::class,
        TripEntity::class,
        PatternEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsPointDao(): GpsPointDao

    abstract fun tripDao(): TripDao

    abstract fun patternDao(): PatternDao
}