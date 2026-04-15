package com.example.transporttracker.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.utils.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AppContainer.provideRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            repository.getTripsCount().collect {
                _uiState.update { state ->
                    state.copy(tripsCount = it)
                }
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
}