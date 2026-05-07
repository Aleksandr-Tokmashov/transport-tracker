package com.example.transporttracker.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsScreen(
    state: AnalyticsUiState
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        AnalyticsCard(
            title = "Всего поездок",
            value = state.totalTrips.toString()
        )

        AnalyticsCard(
            title = "Чаще всего",
            value = state.mostUsedTransport
        )

        Text(
            text = "Инсайты",
            style =
                MaterialTheme.typography.titleLarge
        )

        LazyColumn(
            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            items(state.insights) { insight ->

                Card(
                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                ) {

                    Text(
                        text = insight,
                        modifier =
                            Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    value: String
) {

    Card(
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelLarge
            )

            Text(
                text = value,
                style =
                    MaterialTheme.typography.headlineMedium
            )
        }
    }
}