package com.example.transporttracker.ui.trips

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.transporttracker.R
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.ui.components.TripCard
import com.example.transporttracker.ui.components.localizedName

@Composable
fun TripsScreen(
    trips: List<TripUiState>,
    availableFilters: Set<TransportType> = emptySet(),
    selectedFilter: TransportType? = null,
    onFilterChange: (TransportType?) -> Unit = {},
    onTripClick: (Long) -> Unit = {},
    onCorrectType: (Long, TransportType) -> Unit = { _, _ -> },
    onExportCsv: () -> Unit = {},
    onExportGpx: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (trips.isEmpty() && selectedFilter == null)
                    stringResource(R.string.nav_trips)
                else
                    stringResource(R.string.trips_header_count, trips.size),
                style = MaterialTheme.typography.titleLarge
            )
            if (availableFilters.isNotEmpty()) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.export_cd)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("CSV") },
                            onClick = { menuExpanded = false; onExportCsv() }
                        )
                        DropdownMenuItem(
                            text = { Text("GPX") },
                            onClick = { menuExpanded = false; onExportGpx() }
                        )
                    }
                }
            }
        }

        if (availableFilters.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableFilters.sortedBy { it.name }.forEach { type ->
                    FilterChip(
                        selected = selectedFilter == type,
                        onClick = { onFilterChange(type) },
                        label = { Text(type.localizedName()) }
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
                    text = stringResource(R.string.no_trips_yet),
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
