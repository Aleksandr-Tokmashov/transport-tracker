package com.example.transporttracker.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.domain.usecase.AnalyticsGenerator
import com.example.transporttracker.utils.TripMapper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AnalyticsViewModel(
    repository: TransportRepository
) : ViewModel() {

    private val generator =
        AnalyticsGenerator()

    val uiState = repository
        .getAllTrips()
        .map { trips ->

            val patterns =
                generator.generatePatterns(
                    trips
                )

            AnalyticsUiState(

                insights =
                    patterns.map { it.text },

                totalTrips =
                    trips.size,

                mostUsedTransport =
                    generator.getMostUsedTransport(
                        trips
                    )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started =
                SharingStarted.WhileSubscribed(5000),
            initialValue =
                AnalyticsUiState()
        )
}