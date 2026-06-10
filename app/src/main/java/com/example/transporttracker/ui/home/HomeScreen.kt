package com.example.transporttracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    state: HomeUiState,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onRequestPermission: () -> Unit
) {
    val trackingColor = if (state.isTracking)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(trackingColor.copy(alpha = 0.1f))
                .border(2.dp, trackingColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state.isTracking) Icons.Default.MyLocation else Icons.Default.LocationOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = trackingColor
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (state.isTracking) "Трекинг активен" else "Трекинг остановлен",
            style = MaterialTheme.typography.headlineSmall,
            color = trackingColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${state.tripsCount} поездок",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (state.isTracking) {
            Button(
                onClick = onStopTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stop_tracking_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Остановить")
            }
        } else {
            Button(
                onClick = if (state.needsPermission) onRequestPermission else onStartTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_tracking_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.needsPermission) "Разрешить доступ" else "Начать трекинг"
                )
            }
        }
    }
}
