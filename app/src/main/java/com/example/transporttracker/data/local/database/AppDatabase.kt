package com.example.transporttracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.PatternDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.dao.TripSegmentDao
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.local.entity.PatternEntity
import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.data.local.entity.TripSegmentEntity

@Database(
    entities = [
        GpsPointEntity::class,
        TripEntity::class,
        PatternEntity::class,
        TripSegmentEntity::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gpsPointDao(): GpsPointDao
    abstract fun tripDao(): TripDao
    abstract fun patternDao(): PatternDao
    abstract fun tripSegmentDao(): TripSegmentDao

    companion object {

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `trip_segments` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`tripId` INTEGER NOT NULL, " +
                    "`startTime` INTEGER NOT NULL, " +
                    "`endTime` INTEGER NOT NULL, " +
                    "`transportType` TEXT NOT NULL, " +
                    "`averageSpeed` REAL NOT NULL)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE trips ADD COLUMN distanceMeters REAL NOT NULL DEFAULT 0"
                )
            }
        }

        // Retroactively reclassify BUS trips at metro-level speed (10–33 m/s = 36–120 km/h).
        // The upper bound excludes pre-filter GPS spike data.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "UPDATE trips SET transportType = 'METRO' " +
                    "WHERE transportType = 'BUS' " +
                    "AND averageSpeed > 10.0 AND averageSpeed < 33.3"
                )
            }
        }
    }
}
