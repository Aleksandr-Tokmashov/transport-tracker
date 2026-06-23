package com.example.transporttracker.ui.trips

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.utils.ExportManager
import com.example.transporttracker.utils.TripUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repository: TransportRepository,
    private val exportManager: ExportManager,
    private val tripUiMapper: TripUiMapper
) : ViewModel() {

    private val _filterType = MutableStateFlow<TransportType?>(null)
    val filterType: StateFlow<TransportType?> = _filterType

    private val allMappedTrips = repository.getAllTrips()
        .map { trips -> trips.map { tripUiMapper.map(it) } }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    val availableTypes: StateFlow<Set<TransportType>> = allMappedTrips
        .map { trips -> trips.map { it.transportTypeEnum }.filter { it != TransportType.UNKNOWN }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val trips = combine(allMappedTrips, _filterType) { all, filter ->
        if (filter == null) all else all.filter { it.transportTypeEnum == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportEvent = MutableSharedFlow<Uri>()
    val exportEvent: SharedFlow<Uri> = _exportEvent.asSharedFlow()

    private val _exportGpxEvent = MutableSharedFlow<Uri>()
    val exportGpxEvent: SharedFlow<Uri> = _exportGpxEvent.asSharedFlow()

    fun setFilter(type: TransportType?) {
        _filterType.value = if (_filterType.value == type) null else type
    }

    fun correctTripType(tripId: Long, type: TransportType) {
        viewModelScope.launch {
            repository.updateTripType(tripId, type)
        }
    }

    fun exportTrips() {
        viewModelScope.launch {
            val all = allMappedTrips.replayCache.firstOrNull() ?: emptyList()
            val uri = exportManager.createCsvUri(all)
            _exportEvent.emit(uri)
        }
    }

    fun exportGpx() {
        viewModelScope.launch {
            val all = allMappedTrips.replayCache.firstOrNull() ?: emptyList()
            val uri = exportManager.createGpxUri(all)
            _exportGpxEvent.emit(uri)
        }
    }
}
