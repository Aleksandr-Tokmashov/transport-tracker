package com.example.transporttracker.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transporttracker.service.LocationTrackingService

@Composable
fun HomeScreen(
    state: HomeUiState,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onRequestPermission: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = if (state.isTracking)
                "Tracking ACTIVE"
            else
                "Tracking STOPPED"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            modifier = Modifier.testTag("start_tracking_button"),
            onClick = {
                if (state.needsPermission) {
                    onRequestPermission()
                } else {
                    onStartTracking()
                }
            }
        ) {
            Text("Start Tracking")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.testTag("stop_tracking_button"),
            onClick = onStopTracking
        ) {
            Text("Stop Tracking")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Trips count: ${state.tripsCount}")
    }
}