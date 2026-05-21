package com.example.transporttracker.data.repository

import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.PatternDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.dao.TripSegmentDao
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.PatternEntity
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.data.local.entity.TripSegmentEntity
import com.example.transporttracker.domain.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.transporttracker.utils.TripMapper

class TransportRepository(

    private val gpsPointDao: GpsPointDao,
    private val tripDao: TripDao,
    private val patternDao: PatternDao,
    private val segmentDao: TripSegmentDao
) {

    suspend fun insertGpsPoint(point: GpsPointEntity) {
        gpsPointDao.insertPoint(point)
    }

    fun getAllGpsPoints() =
        gpsPointDao.getAllPoints()

    suspend fun insertTrip(trip: TripEntity): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    suspend fun insertSegment(segment: TripSegmentEntity) {
        segmentDao.insertSegment(segment)
    }

    fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTrips().map { entities ->
            if (entities.isEmpty()) return@map emptyList()
            val tripIds = entities.map { it.id }
            val allSegments = segmentDao.getSegmentsForTrips(tripIds)
            val segmentsByTripId = allSegments.groupBy { it.tripId }
            entities.map { entity ->
                TripMapper.map(entity, segmentsByTripId[entity.id] ?: emptyList())
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
}
