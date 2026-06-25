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
    private val segStartTime: Long = savedStateHandle["startTime"] ?: 0L
    private val segEndTime: Long = savedStateHandle["endTime"] ?: 0L
    private val isSegmentView = segStartTime > 0L && segEndTime > 0L

    private val _points = MutableStateFlow<List<GpsPointEntity>>(emptyList())
    val points: StateFlow<List<GpsPointEntity>> = _points

    private val _summary = MutableStateFlow<TripSummaryState?>(null)
    val summary: StateFlow<TripSummaryState?> = _summary

    init {
        viewModelScope.launch {
            val pointsDeferred = async { repository.getPointsForTrip(tripId) }
            val tripDeferred = async { repository.getTripById(tripId) }

            val allPoints = pointsDeferred.await()
            val filtered = if (isSegmentView) {
                allPoints.filter { it.timestamp in segStartTime..segEndTime }
            } else {
                allPoints
            }
            _points.value = filtered

            val trip = tripDeferred.await() ?: return@launch

            if (isSegmentView) {
                val segments = repository.getSegmentsForTrip(tripId)
                val matchingSeg = segments.firstOrNull { it.startTime <= segStartTime && it.endTime >= segEndTime }
                val segmentType = matchingSeg
                    ?.let { runCatching { TransportType.valueOf(it.transportType) }.getOrDefault(TransportType.UNKNOWN) }
                    ?: runCatching { TransportType.valueOf(trip.transportType) }.getOrDefault(TransportType.UNKNOWN)

                val movingPoints = filtered.filter { it.speed > 1f }
                val avgSpeedMps = if (movingPoints.isNotEmpty()) movingPoints.map { it.speed }.average().toFloat() else 0f

                _summary.value = TripSummaryState(
                    transportType = segmentType,
                    avgSpeedKmh = avgSpeedMps * 3.6f,
                    distanceKm = 0f,
                    durationMin = (segEndTime - segStartTime) / 60_000L
                )
            } else {
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
