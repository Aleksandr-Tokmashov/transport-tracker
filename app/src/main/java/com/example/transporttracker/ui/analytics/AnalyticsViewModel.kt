package com.example.transporttracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.domain.usecase.AnalyticsGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    repository: TransportRepository,
    private val generator: AnalyticsGenerator
) : ViewModel() {

    val uiState = repository
        .getAllTrips()
        .map { trips ->
            val patterns = generator.generatePatterns(trips)
            AnalyticsUiState(
                insights = patterns.map { AnalyticsInsight(text = it.text, count = it.count) },
                totalTrips = trips.size,
                mostUsedTransport = generator.getMostUsedTransport(trips)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState()
        )
}
