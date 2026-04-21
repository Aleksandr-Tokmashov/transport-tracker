package com.example.transporttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.transporttracker.data.local.entity.GpsPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpsPointDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertPoint(point: GpsPointEntity)

    @Query("SELECT * FROM gps_points ORDER BY timestamp DESC")
    fun getAllPoints(): Flow<List<GpsPointEntity>>

    @Query("SELECT * FROM gps_points WHERE tripId = :tripId")
    suspend fun getPointsForTrip(tripId: Long): List<GpsPointEntity>

    @Query("DELETE FROM gps_points")
    suspend fun clearAll()
}