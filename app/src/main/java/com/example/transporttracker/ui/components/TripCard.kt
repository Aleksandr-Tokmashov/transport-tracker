package com.example.transporttracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.transporttracker.ui.trips.TripUiState

@Composable
fun TripCard(
    trip: TripUiState
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = trip.transportType,
                style = MaterialTheme.typography.titleMedium
            )

            if (trip.segments.size > 1) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = trip.segments.joinToString(" → "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Дата: ${trip.date}")

            Text(text = "${trip.startTime} – ${trip.endTime}")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = trip.duration)
                Text(text = trip.averageSpeed)
            }
        }
    }
}
