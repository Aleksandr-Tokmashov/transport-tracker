package com.example.transporttracker.ui.trips

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.utils.AppContainer
import com.example.transporttracker.utils.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AppContainer.provideRepository(application)

    private val _trips =
        MutableStateFlow<List<Trip>>(emptyList())

    val trips: StateFlow<List<Trip>> =
        _trips

    init {

        viewModelScope.launch {

            repository.getAllTrips().collect { entities ->

                _trips.value =
                    entities.map {
                        it.toDomain()
                    }
            }
        }
    }
}