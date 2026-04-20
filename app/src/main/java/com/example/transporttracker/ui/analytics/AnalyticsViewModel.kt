package com.example.transporttracker.ui.analytics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.transporttracker.domain.model.AnalyticsPattern
import com.example.transporttracker.domain.usecase.AnalyticsGenerator
import com.example.transporttracker.utils.AppContainer
import com.example.transporttracker.utils.toDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        AppContainer.provideRepository(application)

    private val generator =
        AnalyticsGenerator()

    private val _patterns =
        MutableStateFlow<List<AnalyticsPattern>>(
            emptyList()
        )

    val patterns:
            StateFlow<List<AnalyticsPattern>> =
        _patterns

    init {

        viewModelScope.launch {

            repository.getAllTrips().collect { entities ->

                val trips =
                    entities.map {
                        it.toDomain()
                    }

                _patterns.value =
                    generator.generatePatterns(trips)
            }
        }
    }
}