package com.example.transporttracker.data.repository

import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.PatternDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.PatternEntity
import com.example.transporttracker.data.local.entity.TripEntity

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

    fun getAllTrips() =
        tripDao.getAllTrips()

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