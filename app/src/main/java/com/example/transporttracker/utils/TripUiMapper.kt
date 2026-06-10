package com.example.transporttracker.utils

import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.ui.trips.TripUiState

object TripUiMapper {

    fun map(trip: Trip): TripUiState {

        return TripUiState(
            id = trip.id,
            date = TripFormatter.formatDate(trip.startTime),
            startTime = TripFormatter.formatTimeOnly(trip.startTime),
            endTime = TripFormatter.formatTimeOnly(trip.endTime),
            duration = TripFormatter.formatDuration(trip.startTime, trip.endTime),
            transportType = trip.transportType.displayName(),
            averageSpeed = TripFormatter.formatSpeed(trip.averageSpeed),
            segments = trip.segments.map { it.transportType.displayName() },
            transportTypeEnum = trip.transportType,
            segmentTypes = trip.segments.map { it.transportType }
        )
    }

    private fun TransportType.displayName(): String = when (this) {
        TransportType.WALK -> "Пешком"
        TransportType.BUS -> "Автобус"
        TransportType.TRAM -> "Трамвай"
        TransportType.METRO -> "Метро"
        TransportType.MCD -> "МЦД"
        TransportType.UNKNOWN -> "Неизвестно"
    }
}
