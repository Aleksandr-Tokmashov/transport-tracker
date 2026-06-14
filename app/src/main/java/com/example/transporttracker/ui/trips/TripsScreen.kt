package com.example.transporttracker.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.ui.components.TripCard

@Composable
fun TripsScreen(
    trips: List<TripUiState>,
    onTripClick: (Long) -> Unit = {},
    onCorrectType: (Long, TransportType) -> Unit = { _, _ -> },
    onExport: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (trips.isEmpty()) "Поездки" else "Поездки (${trips.size})",
                style = MaterialTheme.typography.titleLarge
            )
            if (trips.isNotEmpty()) {
                IconButton(onClick = onExport) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Экспортировать"
                    )
                }
            }
        }

        if (trips.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Поездок пока нет",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
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
    }
}
