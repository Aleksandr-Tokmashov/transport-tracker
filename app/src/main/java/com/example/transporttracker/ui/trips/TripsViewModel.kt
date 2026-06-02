package com.example.transporttracker.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.utils.TripUiMapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TripsViewModel(
    private val repository: TransportRepository
) : ViewModel() {

    val trips = repository
        .getAllTrips()
        .map { trips -> trips.map { TripUiMapper.map(it) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun correctTripType(tripId: Long, type: TransportType) {
        viewModelScope.launch {
            repository.updateTripType(tripId, type)
        }
    }
}
