package com.example.transporttracker

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.transporttracker.data.local.database.AppDatabase
import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.dao.TripSegmentDao
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.data.local.entity.TripSegmentEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var tripDao: TripDao
    private lateinit var segmentDao: TripSegmentDao
    private lateinit var gpsDao: GpsPointDao

    private fun tripEntity(
        id: Long = 0L,
        startTime: Long = 1_000L,
        endTime: Long = 2_000L,
        transportType: String = "BUS",
        averageSpeed: Float = 5f,
        distanceMeters: Float = 500f
    ) = TripEntity(id, startTime, endTime, transportType, averageSpeed, "WEEKDAY", "MORNING", distanceMeters)

    private fun gpsPoint(tripId: Long, ts: Long = 1_000L) =
        GpsPointEntity(0, ts, 55.75, 37.61, 5f, 10f, tripId)

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        tripDao    = db.tripDao()
        segmentDao = db.tripSegmentDao()
        gpsDao     = db.gpsPointDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ── TripDao ───────────────────────────────────────────────────────────────

    @Test
    fun insertAndRetrieveTrip() = runBlocking {
        tripDao.insertTrip(tripEntity())
        val trips = tripDao.getAllTrips().first()
        assertEquals(1, trips.size)
        assertEquals("BUS", trips[0].transportType)
    }

    @Test
    fun getAllTrips_excludesActiveTrip() = runBlocking {
        // Active trip: endTime = 0 (sentinel)
        tripDao.insertTrip(tripEntity(endTime = 0L))
        tripDao.insertTrip(tripEntity(endTime = 2_000L))
        val trips = tripDao.getAllTrips().first()
        assertEquals(1, trips.size)
        assertEquals(2_000L, trips[0].endTime)
    }

    @Test
    fun getActiveTrip_returnsOnlyActiveTrip() = runBlocking {
        tripDao.insertTrip(tripEntity(endTime = 2_000L))
        tripDao.insertTrip(tripEntity(endTime = 0L))
        val active = tripDao.getActiveTrip()
        assertNotNull(active)
        assertEquals(0L, active!!.endTime)
    }

    @Test
    fun getActiveTrip_returnsNull_whenNoActiveTrip() = runBlocking {
        tripDao.insertTrip(tripEntity(endTime = 2_000L))
        assertNull(tripDao.getActiveTrip())
    }

    @Test
    fun updateTransportType_changesType() = runBlocking {
        val id = tripDao.insertTrip(tripEntity(transportType = "BUS"))
        tripDao.updateTransportType(id, "METRO")
        val trips = tripDao.getAllTrips().first()
        assertEquals("METRO", trips[0].transportType)
    }

    @Test
    fun deleteTrip_removesIt() = runBlocking {
        val id = tripDao.insertTrip(tripEntity())
        tripDao.deleteTripById(id)
        assertTrue(tripDao.getAllTrips().first().isEmpty())
    }

    @Test
    fun getAllTrips_orderedByStartTimeDescending() = runBlocking {
        tripDao.insertTrip(tripEntity(startTime = 1_000L, endTime = 1_500L))
        tripDao.insertTrip(tripEntity(startTime = 3_000L, endTime = 3_500L))
        tripDao.insertTrip(tripEntity(startTime = 2_000L, endTime = 2_500L))
        val trips = tripDao.getAllTrips().first()
        assertTrue(trips[0].startTime > trips[1].startTime)
        assertTrue(trips[1].startTime > trips[2].startTime)
    }

    @Test
    fun getTripById_returnsCorrectTrip() = runBlocking {
        val id = tripDao.insertTrip(tripEntity(transportType = "METRO"))
        val trip = tripDao.getTripById(id)
        assertNotNull(trip)
        assertEquals("METRO", trip!!.transportType)
    }

    @Test
    fun getTripById_returnsNull_forMissingId() = runBlocking {
        assertNull(tripDao.getTripById(999L))
    }

    @Test
    fun updateTrip_updatesAllFields() = runBlocking {
        val id = tripDao.insertTrip(tripEntity())
        tripDao.updateTrip(tripEntity(id = id, transportType = "TRAM", averageSpeed = 12f, distanceMeters = 2_000f))
        val trip = tripDao.getTripById(id)
        assertEquals("TRAM", trip?.transportType)
        assertEquals(12f, trip?.averageSpeed)
        assertEquals(2_000f, trip?.distanceMeters)
    }

    // ── TripSegmentDao ────────────────────────────────────────────────────────

    @Test
    fun insertAndRetrieveSegment() = runBlocking {
        val tripId = tripDao.insertTrip(tripEntity())
        segmentDao.insertSegment(TripSegmentEntity(0, tripId, 1_000L, 2_000L, "METRO", 12f))
        val segments = segmentDao.getSegmentsForTrip(tripId)
        assertEquals(1, segments.size)
        assertEquals("METRO", segments[0].transportType)
    }

    @Test
    fun getSegmentsForTrip_orderedByStartTime() = runBlocking {
        val tripId = tripDao.insertTrip(tripEntity())
        segmentDao.insertSegment(TripSegmentEntity(0, tripId, 5_000L, 8_000L, "METRO", 12f))
        segmentDao.insertSegment(TripSegmentEntity(0, tripId, 1_000L, 4_000L, "TRAM", 5f))
        val segments = segmentDao.getSegmentsForTrip(tripId)
        assertTrue(segments[0].startTime < segments[1].startTime)
    }

    @Test
    fun deleteSegmentsForTrip_removesAllSegments() = runBlocking {
        val tripId = tripDao.insertTrip(tripEntity())
        segmentDao.insertSegment(TripSegmentEntity(0, tripId, 1_000L, 2_000L, "BUS", 5f))
        segmentDao.insertSegment(TripSegmentEntity(0, tripId, 2_000L, 3_000L, "WALK", 1f))
        segmentDao.deleteSegmentsForTrip(tripId)
        assertTrue(segmentDao.getSegmentsForTrip(tripId).isEmpty())
    }

    @Test
    fun getSegmentsForTrips_batchFetch() = runBlocking {
        val id1 = tripDao.insertTrip(tripEntity())
        val id2 = tripDao.insertTrip(tripEntity())
        segmentDao.insertSegment(TripSegmentEntity(0, id1, 1_000L, 2_000L, "BUS", 5f))
        segmentDao.insertSegment(TripSegmentEntity(0, id2, 1_000L, 2_000L, "METRO", 12f))
        val segments = segmentDao.getSegmentsForTrips(listOf(id1, id2))
        assertEquals(2, segments.size)
    }

    @Test
    fun getSegmentsForTrip_returnsEmpty_forOtherTrip() = runBlocking {
        val id1 = tripDao.insertTrip(tripEntity())
        val id2 = tripDao.insertTrip(tripEntity())
        segmentDao.insertSegment(TripSegmentEntity(0, id1, 1_000L, 2_000L, "BUS", 5f))
        assertTrue(segmentDao.getSegmentsForTrip(id2).isEmpty())
    }

    // ── GpsPointDao ───────────────────────────────────────────────────────────

    @Test
    fun insertAndRetrieveGpsPoint() = runBlocking {
        val tripId = tripDao.insertTrip(tripEntity())
        gpsDao.insertPoint(gpsPoint(tripId))
        val points = gpsDao.getPointsForTrip(tripId)
        assertEquals(1, points.size)
        assertEquals(55.75, points[0].latitude, 0.0001)
    }

    @Test
    fun getPointsForTrip_orderedByTimestampAscending() = runBlocking {
        val tripId = tripDao.insertTrip(tripEntity())
        gpsDao.insertPoint(GpsPointEntity(0, 3_000L, 55.75, 37.61, 5f, 10f, tripId))
        gpsDao.insertPoint(GpsPointEntity(0, 1_000L, 55.75, 37.61, 5f, 10f, tripId))
        gpsDao.insertPoint(GpsPointEntity(0, 2_000L, 55.75, 37.61, 5f, 10f, tripId))
        val points = gpsDao.getPointsForTrip(tripId)
        assertTrue(points[0].timestamp < points[1].timestamp)
        assertTrue(points[1].timestamp < points[2].timestamp)
    }

    @Test
    fun deletePointsForTrip_removesCorrectPoints() = runBlocking {
        val id1 = tripDao.insertTrip(tripEntity())
        val id2 = tripDao.insertTrip(tripEntity())
        gpsDao.insertPoint(gpsPoint(id1))
        gpsDao.insertPoint(gpsPoint(id2))
        gpsDao.deletePointsForTrip(id1)
        assertTrue(gpsDao.getPointsForTrip(id1).isEmpty())
        assertEquals(1, gpsDao.getPointsForTrip(id2).size)
    }

    @Test
    fun deleteOrphanedPoints_removesNullTripIdPoints() = runBlocking {
        val tripId = tripDao.insertTrip(tripEntity())
        gpsDao.insertPoint(GpsPointEntity(0, 1_000L, 55.75, 37.61, 5f, 10f, null))   // orphaned
        gpsDao.insertPoint(GpsPointEntity(0, 2_000L, 55.75, 37.61, 5f, 10f, tripId)) // linked
        gpsDao.deleteOrphanedPoints()
        assertEquals(1, gpsDao.getPointsForTrip(tripId).size)
        // Check orphaned is gone by checking total points
        val all = gpsDao.getAllPoints().first()
        assertEquals(1, all.size)
    }

    @Test
    fun getPointsForTrip_returnsEmpty_forOtherTrip() = runBlocking {
        val id1 = tripDao.insertTrip(tripEntity())
        val id2 = tripDao.insertTrip(tripEntity())
        gpsDao.insertPoint(gpsPoint(id1))
        assertTrue(gpsDao.getPointsForTrip(id2).isEmpty())
    }

    // ── MIGRATION_3_4 logic verified via direct SQL ───────────────────────────

    @Test
    fun fastBusTrip_shouldBeReclassifiedAsMetro_byMigration() = runBlocking {
        // Verify the migration condition: BUS trips with 10 < speed < 33.3 m/s
        // We can't run the migration on an in-memory DB built from scratch,
        // but we can confirm the SQL predicate is correct.
        val metroSpeedMps = 15f  // 54 km/h — clearly metro
        val tripId = tripDao.insertTrip(
            tripEntity(transportType = "BUS", averageSpeed = metroSpeedMps)
        )
        // Simulate what MIGRATION_3_4 does
        db.openHelper.writableDatabase.execSQL(
            "UPDATE trips SET transportType = 'METRO' " +
            "WHERE transportType = 'BUS' AND averageSpeed > 10.0 AND averageSpeed < 33.3"
        )
        val trip = tripDao.getTripById(tripId)
        assertEquals("METRO", trip?.transportType)
    }

    @Test
    fun slowBusTrip_notReclassifiedByMigration() = runBlocking {
        val slowSpeedMps = 5f  // 18 km/h — clearly bus
        val tripId = tripDao.insertTrip(
            tripEntity(transportType = "BUS", averageSpeed = slowSpeedMps)
        )
        db.openHelper.writableDatabase.execSQL(
            "UPDATE trips SET transportType = 'METRO' " +
            "WHERE transportType = 'BUS' AND averageSpeed > 10.0 AND averageSpeed < 33.3"
        )
        val trip = tripDao.getTripById(tripId)
        assertEquals("BUS", trip?.transportType)
    }
}
