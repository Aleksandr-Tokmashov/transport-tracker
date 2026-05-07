package com.example.transporttracker.ui.trips

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TripsRoute(
    viewModel: TripsViewModel = viewModel()
) {

    val trips by viewModel
        .trips
        .collectAsStateWithLifecycle()

    TripsScreen(
        trips = trips
    )
}