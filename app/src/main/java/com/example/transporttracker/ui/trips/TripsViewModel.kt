package com.example.transporttracker.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.utils.TripMapper
import com.example.transporttracker.utils.TripUiMapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TripsViewModel(
    repository: TransportRepository
) : ViewModel() {

    val trips = repository
        .getAllTrips()
        .map { trips ->

            trips.map { trip ->

                TripUiMapper.map(trip)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started =
                SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}