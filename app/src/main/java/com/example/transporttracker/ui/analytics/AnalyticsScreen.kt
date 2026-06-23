package com.example.transporttracker.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

        if (state.weekStats.tripCount > 0 || state.monthStats.tripCount > 0) {
            item { PeriodStatsCard(weekStats = state.weekStats, monthStats = state.monthStats) }
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

@Composable
private fun PeriodStatsCard(weekStats: PeriodStats, monthStats: PeriodStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_period_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeriodColumn(
                    label = stringResource(R.string.analytics_week),
                    stats = weekStats,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                PeriodColumn(
                    label = stringResource(R.string.analytics_month),
                    stats = monthStats,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PeriodColumn(label: String, stats: PeriodStats, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.analytics_period_trips, stats.tripCount),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(R.string.analytics_period_detail, stats.distanceKm, stats.durationMin),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
