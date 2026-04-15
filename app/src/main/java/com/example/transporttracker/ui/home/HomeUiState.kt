package com.example.transporttracker.ui.home

data class HomeUiState(
    val isTracking: Boolean = false,
    val tripsCount: Int = 0,
    val needsPermission: Boolean = false
)