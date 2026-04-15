package com.example.transporttracker.ui.home

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transporttracker.service.LocationTrackingService

@Composable
fun HomeRoute(viewModel: HomeViewModel = viewModel()) {

    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted = result.values.all { it }

            viewModel.setPermissionNeeded(!granted)
        }

    HomeScreen(
        state = state,
        onStartTracking = {
            viewModel.startTracking()

            ContextCompat.startForegroundService(
                context,
                Intent(context, LocationTrackingService::class.java)
            )
        },
        onStopTracking = {
            viewModel.stopTracking()
            context.stopService(
                Intent(context, LocationTrackingService::class.java)
            )
        },
        onRequestPermission = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    )
}