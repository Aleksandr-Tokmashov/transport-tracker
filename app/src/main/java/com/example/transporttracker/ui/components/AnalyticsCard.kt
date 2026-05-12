package com.example.transporttracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.transporttracker.ui.analytics.AnalyticsInsight

@Composable
fun AnalyticsCard(
    insight: AnalyticsInsight
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Column {

                Text(
                    text = insight.text,
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge
                )
            }

            Text(
                text = "${insight.count}",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )
        }
    }
}