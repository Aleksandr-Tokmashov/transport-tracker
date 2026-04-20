package com.example.transporttracker.ui.trips

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.transporttracker.utils.DateUtils

@Composable
fun TripsScreen(
    viewModel: TripsViewModel = viewModel()
) {

    val trips by viewModel.trips.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        items(trips) { trip ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Transport: ${trip.transportType}"
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Start: ${
                                DateUtils.formatTime(
                                    trip.startTime
                                )
                            }"
                    )

                    Text(
                        text =
                            "End: ${
                                DateUtils.formatTime(
                                    trip.endTime
                                )
                            }"
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text =
                            "Average speed: ${
                                "%.2f".format(
                                    trip.averageSpeed
                                )
                            } m/s"
                    )
                }
            }
        }
    }
}