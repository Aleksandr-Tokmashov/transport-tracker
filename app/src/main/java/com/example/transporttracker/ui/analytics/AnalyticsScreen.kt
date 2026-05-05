package com.example.transporttracker.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {

    val uiState by
    viewModel.uiState.collectAsState()

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        item {

            SummaryCard(
                totalTrips =
                    uiState.totalTrips,
                mostUsedTransport =
                    uiState.mostUsedTransport
            )
        }

        items(uiState.insights) { insight ->

            InsightCard(insight)
        }
    }
}

@Composable
private fun SummaryCard(
    totalTrips: Int,
    mostUsedTransport: String
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "Всего поездок: $totalTrips",
                style =
                    MaterialTheme.typography.titleMedium
            )

            Text(
                text =
                    "Самый частый транспорт: $mostUsedTransport"
            )
        }
    }
}

@Composable
private fun InsightCard(
    insight: String
) {

    Card(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text = insight,
            modifier =
                Modifier.padding(16.dp)
        )
    }
}