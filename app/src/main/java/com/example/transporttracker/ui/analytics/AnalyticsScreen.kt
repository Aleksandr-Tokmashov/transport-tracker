package com.example.transporttracker.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.transporttracker.R
import com.example.transporttracker.ui.components.AnalyticsCard
import com.example.transporttracker.ui.components.TimeBinChart
import com.example.transporttracker.ui.components.TransportShareChart

@Composable
fun AnalyticsScreen(
    state: AnalyticsUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.total_trips, state.totalTrips),
                    style = MaterialTheme.typography.headlineSmall
                )
                if (state.totalDistanceKm > 0f) {
                    Text(
                        text = stringResource(R.string.analytics_total_distance, state.totalDistanceKm),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state.mostUsedTransport.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.most_used_transport, state.mostUsedTransport),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            }
        }

        if (state.transportShares.isNotEmpty()) {
            item { TransportShareChart(state.transportShares) }
        }

        if (state.timeBinCounts.any { it.count > 0 }) {
            item { TimeBinChart(state.timeBinCounts) }
        }

        items(state.insights) { insight ->
            AnalyticsCard(insight = insight)
        }
    }
}
