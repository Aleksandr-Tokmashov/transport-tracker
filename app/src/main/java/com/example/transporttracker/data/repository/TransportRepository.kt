package com.example.transporttracker.data.repository

import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.PatternDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.dao.TripSegmentDao
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.PatternEntity
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.data.local.entity.TripSegmentEntity
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.transporttracker.utils.TripMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportRepository @Inject constructor(

    private val gpsPointDao: GpsPointDao,
    private val tripDao: TripDao,
    private val patternDao: PatternDao,
    private val segmentDao: TripSegmentDao
) {

    suspend fun insertGpsPoint(point: GpsPointEntity) {
        gpsPointDao.insertPoint(point)
    }

    suspend fun getPointsForTrip(tripId: Long): List<GpsPointEntity> {
        return gpsPointDao.getPointsForTrip(tripId)
    }

    suspend fun insertTrip(trip: TripEntity): Long {
        return tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    suspend fun getActiveTrip(): TripEntity? {
        return tripDao.getActiveTrip()
    }

    suspend fun deleteAbandonedTrip(tripId: Long) {
        gpsPointDao.deletePointsForTrip(tripId)
        segmentDao.deleteSegmentsForTrip(tripId)
        tripDao.deleteTripById(tripId)
    }

    suspend fun deleteOrphanedGpsPoints() {
        gpsPointDao.deleteOrphanedPoints()
    }

    suspend fun updateTripType(tripId: Long, type: TransportType) {
        tripDao.updateTransportType(tripId, type.name)
    }

    suspend fun getTripById(tripId: Long): TripEntity? = tripDao.getTripById(tripId)

    suspend fun insertSegment(segment: TripSegmentEntity) {
        segmentDao.insertSegment(segment)
    }

    suspend fun getSegmentsForTrip(tripId: Long): List<TripSegmentEntity> {
        return segmentDao.getSegmentsForTrip(tripId)
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

    fun getTripsCount() = tripDao.getTripsCount()

    suspend fun insertPattern(pattern: PatternEntity) {
        patternDao.insertPattern(pattern)
    }

    fun getAllPatterns() = patternDao.getAllPatterns()

    fun getAllGpsPoints() = gpsPointDao.getAllPoints()
}
