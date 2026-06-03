package com.example.transporttracker.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.repository.TransportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: TransportRepository,
    private val tripId: Long
) : ViewModel() {

    private val _points = MutableStateFlow<List<GpsPointEntity>>(emptyList())
    val points: StateFlow<List<GpsPointEntity>> = _points

    init {
        viewModelScope.launch {
            _points.value = repository.getPointsForTrip(tripId)
        }
    }
}
