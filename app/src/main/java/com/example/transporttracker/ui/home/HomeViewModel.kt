package com.example.transporttracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.repository.TransportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TransportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.getTripsCount().collect {
                _uiState.update { state -> state.copy(tripsCount = it) }
            }
        }
    }

    fun startTracking() {
        _uiState.update { it.copy(isTracking = true) }
    }

    fun stopTracking() {
        _uiState.update { it.copy(isTracking = false) }
    }

    fun setPermissionNeeded(value: Boolean) {
        _uiState.update { it.copy(needsPermission = value) }
    }

    fun refreshTrackingState() {
        _uiState.update { it.copy(isTracking = TrackingState.isTracking) }
    }
}

object TrackingState {
    var isTracking = false
}
