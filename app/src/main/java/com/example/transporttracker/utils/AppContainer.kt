package com.example.transporttracker.utils

import android.content.Context
import com.example.transporttracker.data.local.database.DatabaseProvider
import com.example.transporttracker.data.repository.TransportRepository

object AppContainer {

    private var repository: TransportRepository? = null

    fun provideRepository(context: Context): TransportRepository {

        return repository ?: synchronized(this) {

            val database = DatabaseProvider.getDatabase(context)

            val instance = TransportRepository(
                gpsPointDao = database.gpsPointDao(),
                tripDao = database.tripDao(),
                patternDao = database.patternDao(),
                segmentDao = database.tripSegmentDao()
            )

            repository = instance

            instance
        }
    }
}
