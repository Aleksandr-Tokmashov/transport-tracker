package com.example.transporttracker.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.domain.model.TransportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripSummaryState(
    val transportType: TransportType,
    val avgSpeedKmh: Float,
    val distanceKm: Float,
    val durationMin: Long
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: TransportRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = checkNotNull(savedStateHandle["tripId"])

    private val _points = MutableStateFlow<List<GpsPointEntity>>(emptyList())
    val points: StateFlow<List<GpsPointEntity>> = _points

    private val _summary = MutableStateFlow<TripSummaryState?>(null)
    val summary: StateFlow<TripSummaryState?> = _summary

    init {
        viewModelScope.launch {
            val pointsDeferred = async { repository.getPointsForTrip(tripId) }
            val tripDeferred = async { repository.getTripById(tripId) }

            _points.value = pointsDeferred.await()

            val trip = tripDeferred.await()
            if (trip != null) {
                _summary.value = TripSummaryState(
                    transportType = runCatching { TransportType.valueOf(trip.transportType) }
                        .getOrDefault(TransportType.UNKNOWN),
                    avgSpeedKmh = trip.averageSpeed * 3.6f,
                    distanceKm = trip.distanceMeters / 1000f,
                    durationMin = (trip.endTime - trip.startTime) / 60_000L
                )
            }
        }
    }
}
