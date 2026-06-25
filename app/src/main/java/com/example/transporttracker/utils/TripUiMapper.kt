package com.example.transporttracker.utils

import android.content.Context
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.domain.model.TransportType
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

    // Returns one card per meaningful transport segment; falls back to a single
    // card for the whole trip when there are fewer than two non-walk legs.
    fun mapToCards(trip: Trip): List<TripUiState> {
        val legs = trip.segments.filter {
            it.transportType != TransportType.WALK && it.transportType != TransportType.UNKNOWN
        }
        if (legs.size < 2) return listOf(map(trip))

        return legs.map { seg ->
            TripUiState(
                id = trip.id,
                date = formatter.formatDate(seg.startTime),
                startTime = formatter.formatTimeOnly(seg.startTime),
                endTime = formatter.formatTimeOnly(seg.endTime),
                duration = formatter.formatDuration(seg.startTime, seg.endTime),
                transportType = seg.transportType.localizedName(context),
                averageSpeed = formatter.formatSpeed(seg.averageSpeed),
                transportTypeEnum = seg.transportType,
                segmentStartTime = seg.startTime,
                segmentEndTime = seg.endTime
            )
        }
    }
}
