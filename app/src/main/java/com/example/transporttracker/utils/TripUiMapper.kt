package com.example.transporttracker.utils

import android.content.Context
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.ui.components.localizedName
import com.example.transporttracker.ui.trips.TripUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripUiMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val formatter: TripFormatter
) {
    fun map(trip: Trip): TripUiState = TripUiState(
        id = trip.id,
        date = formatter.formatDate(trip.startTime),
        startTime = formatter.formatTimeOnly(trip.startTime),
        endTime = formatter.formatTimeOnly(trip.endTime),
        duration = formatter.formatDuration(trip.startTime, trip.endTime),
        transportType = trip.transportType.localizedName(context),
        averageSpeed = formatter.formatSpeed(trip.averageSpeed),
        segments = trip.segments.map { it.transportType.localizedName(context) },
        transportTypeEnum = trip.transportType,
        segmentTypes = trip.segments.map { it.transportType },
        distance = if (trip.distanceMeters > 0f) formatter.formatDistance(trip.distanceMeters) else ""
    )
}
