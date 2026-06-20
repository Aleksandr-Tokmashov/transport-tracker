package com.example.transporttracker.di

import android.content.Context
import androidx.room.Room
import com.example.transporttracker.data.local.dao.GpsPointDao
import com.example.transporttracker.data.local.dao.PatternDao
import com.example.transporttracker.data.local.dao.TripDao
import com.example.transporttracker.data.local.dao.TripSegmentDao
import com.example.transporttracker.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "transport_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGpsPointDao(db: AppDatabase): GpsPointDao = db.gpsPointDao()

    @Provides
    fun provideTripDao(db: AppDatabase): TripDao = db.tripDao()

    @Provides
    fun providePatternDao(db: AppDatabase): PatternDao = db.patternDao()

    @Provides
    fun provideTripSegmentDao(db: AppDatabase): TripSegmentDao = db.tripSegmentDao()
}
