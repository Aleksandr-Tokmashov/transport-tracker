package com.example.transporttracker.ui.trips

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transporttracker.ui.components.TripCard

@Composable
fun TripsScreen(
    viewModel: TripsViewModel
) {

    val trips by viewModel
        .trips
        .collectAsState()

    if (trips.isEmpty()) {

        Text(
            text = "Поездок пока нет"
        )

        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        items(trips) { trip ->

            TripCard(
                trip = trip
            )
        }
    }
}