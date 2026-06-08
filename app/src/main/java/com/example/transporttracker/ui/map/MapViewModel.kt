package com.example.transporttracker.ui.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.data.repository.TransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: TransportRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = checkNotNull(savedStateHandle["tripId"])

    private val _points = MutableStateFlow<List<GpsPointEntity>>(emptyList())
    val points: StateFlow<List<GpsPointEntity>> = _points

    init {
        viewModelScope.launch {
            _points.value = repository.getPointsForTrip(tripId)
        }
    }
}
