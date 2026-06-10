package com.example.transporttracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val transportType = trip.transportTypeEnum
    val color = transportType.color()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onTripClick,
                onLongClick = { showCorrectionDialog = true }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transportType.icon(),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = trip.transportType,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = trip.duration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${trip.date}  ${trip.startTime} – ${trip.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = trip.averageSpeed,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (trip.segmentTypes.size > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SegmentChipsRow(
                        segments = trip.segments,
                        segmentTypes = trip.segmentTypes
                    )
                }
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
private fun SegmentChipsRow(
    segments: List<String>,
    segmentTypes: List<TransportType>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        segments.zip(segmentTypes).forEachIndexed { index, (label, type) ->
            if (index > 0) {
                Text(
                    text = "→",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SegmentChip(label = label, transportType = type)
        }
    }
}

@Composable
private fun SegmentChip(label: String, transportType: TransportType) {
    val color = transportType.color()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = transportType.icon(),
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = type.icon(),
                                contentDescription = null,
                                tint = type.color(),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(text = type.displayName())
                        }
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

fun TransportType.displayName(): String = when (this) {
    TransportType.WALK -> "Пешком"
    TransportType.BUS -> "Автобус"
    TransportType.TRAM -> "Трамвай"
    TransportType.METRO -> "Метро"
    TransportType.MCD -> "МЦД"
    TransportType.UNKNOWN -> "Неизвестно"
}
