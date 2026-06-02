package com.example.transporttracker.ui.trips

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.ui.components.TripCard

@Composable
fun TripsScreen(
    trips: List<TripUiState>,
    onTripClick: (Long) -> Unit = {},
    onCorrectType: (Long, TransportType) -> Unit = { _, _ -> }
) {

    if (trips.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Поездок пока нет")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = trips, key = { it.id }) { trip ->
            TripCard(
                trip = trip,
                onTripClick = { onTripClick(trip.id) },
                onCorrectType = { type -> onCorrectType(trip.id, type) }
            )
        }
    }
}
