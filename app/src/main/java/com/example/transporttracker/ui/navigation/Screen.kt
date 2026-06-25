package com.example.transporttracker.ui.navigation

sealed class Screen(val route: String) {

    data object Home : Screen("home")
    data object Trips : Screen("trips")
    data object Analytics : Screen("analytics")
    data object TripMap : Screen("trips/{tripId}?startTime={startTime}&endTime={endTime}") {
        fun createRoute(tripId: Long, startTime: Long = 0L, endTime: Long = 0L) =
            "trips/$tripId?startTime=$startTime&endTime=$endTime"
    }
}
