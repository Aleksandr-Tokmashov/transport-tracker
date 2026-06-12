package com.example.transporttracker.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.transporttracker.service.LocationTrackingService

@Composable
fun HomeRoute(viewModel: HomeViewModel = hiltViewModel()) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            viewModel.setPermissionNeeded(!result.values.all { it })
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
            context.stopService(Intent(context, LocationTrackingService::class.java))
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

    // Request battery optimization exemption when tracking starts
    LaunchedEffect(state.isTracking) {
        if (state.isTracking) {
            val pm = context.getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshTrackingState()
    }

    LaunchedEffect(Unit) {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        viewModel.setPermissionNeeded(!hasLocationPermission)

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    }
}
