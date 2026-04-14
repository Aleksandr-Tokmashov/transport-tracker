package com.example.transporttracker.ui.navigation

sealed class Screen(val route: String) {

    data object Home : Screen("home")
    data object Trips : Screen("trips")
    data object Analytics : Screen("analytics")
}