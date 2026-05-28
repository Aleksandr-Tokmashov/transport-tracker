package com.example.transporttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.transporttracker.data.local.entity.TripSegmentEntity

@Dao
interface TripSegmentDao {

    @Insert
    suspend fun insertSegment(segment: TripSegmentEntity): Long

    @Query("SELECT * FROM trip_segments WHERE tripId = :tripId ORDER BY startTime ASC")
    suspend fun getSegmentsForTrip(tripId: Long): List<TripSegmentEntity>

    @Query("SELECT * FROM trip_segments WHERE tripId IN (:tripIds) ORDER BY startTime ASC")
    suspend fun getSegmentsForTrips(tripIds: List<Long>): List<TripSegmentEntity>
}
