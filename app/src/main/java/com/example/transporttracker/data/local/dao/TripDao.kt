package com.example.transporttracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.transporttracker.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Upsert
    suspend fun insertTrip(trip: TripEntity): Long

    @Query("SELECT * FROM trips ORDER BY startTime DESC")
    fun getAllTrips(): Flow<List<TripEntity>>

    @Query("SELECT COUNT(*) FROM trips")
    fun getTripsCount(): Flow<Int>

    // endTime = 0 is the sentinel for a trip that was active when the service died
    @Query("SELECT * FROM trips WHERE endTime = 0 LIMIT 1")
    suspend fun getActiveTrip(): TripEntity?

    @Query("UPDATE trips SET transportType = :type WHERE id = :id")
    suspend fun updateTransportType(id: Long, type: String)

    @Query("DELETE FROM trips")
    suspend fun clearAll()

    @Update
    suspend fun updateTrip(trip: TripEntity)
}
