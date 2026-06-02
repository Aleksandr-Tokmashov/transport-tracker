package com.example.transporttracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.ui.trips.TripUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TripCard(
    trip: TripUiState,
    onTripClick: () -> Unit = {},
    onCorrectType: (TransportType) -> Unit = {}
) {

    var showCorrectionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onTripClick,
                onLongClick = { showCorrectionDialog = true }
            ),
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

    if (showCorrectionDialog) {
        TransportTypeCorrectionDialog(
            onDismiss = { showCorrectionDialog = false },
            onSelect = { type ->
                onCorrectType(type)
                showCorrectionDialog = false
            }
        )
    }
}

@Composable
private fun TransportTypeCorrectionDialog(
    onDismiss: () -> Unit,
    onSelect: (TransportType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Исправить тип транспорта") },
        text = {
            Column {
                TransportType.entries.forEach { type ->
                    TextButton(
                        onClick = { onSelect(type) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = type.displayName(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

private fun TransportType.displayName(): String = when (this) {
    TransportType.WALK -> "Пешком"
    TransportType.BUS -> "Автобус"
    TransportType.TRAM -> "Трамвай"
    TransportType.METRO -> "Метро"
    TransportType.MCD -> "МЦД"
    TransportType.UNKNOWN -> "Неизвестно"
}
