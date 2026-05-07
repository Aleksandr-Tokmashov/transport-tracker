package com.example.transporttracker.data.repository

import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.PatternDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.PatternEntity
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.domain.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.transporttracker.utils.TripMapper

class TransportRepository(

    private val gpsPointDao: GpsPointDao,
    private val tripDao: TripDao,
    private val patternDao: PatternDao
) {

    suspend fun insertGpsPoint(point: GpsPointEntity) {
        gpsPointDao.insertPoint(point)
    }

    fun getAllGpsPoints() =
        gpsPointDao.getAllPoints()

    suspend fun insertTrip(trip: TripEntity): Long {
        return tripDao.insertTrip(trip)
    }

    fun getAllTrips(): Flow<List<Trip>> {

        return tripDao
            .getAllTrips()
            .map { entities ->

                entities.map { entity ->
                    TripMapper.map(entity)
                }
            }
    }

    fun getTripsCount() =
        tripDao.getTripsCount()

    suspend fun insertPattern(pattern: PatternEntity) {
        patternDao.insertPattern(pattern)
    }

    fun getAllPatterns() =
        patternDao.getAllPatterns()

    suspend fun updateTrip(
        trip: TripEntity
    ) {

        tripDao.updateTrip(trip)
    }
}