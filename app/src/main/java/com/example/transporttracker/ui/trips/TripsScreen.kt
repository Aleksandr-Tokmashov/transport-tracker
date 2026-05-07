package com.example.transporttracker.ui.trips

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transporttracker.ui.components.TripCard
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

@Composable
fun TripsScreen(
    trips: List<TripUiState>
) {

    if (trips.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = "Поездок пока нет"
            )
        }

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        items(
            items = trips,
            key = { it.id }
        ) { trip ->

            TripCard(
                trip = trip
            )
        }
    }
}