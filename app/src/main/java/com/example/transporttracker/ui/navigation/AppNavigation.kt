package com.example.transporttracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.transporttracker.ui.analytics.AnalyticsScreen
import com.example.transporttracker.ui.home.HomeRoute
import com.example.transporttracker.ui.home.HomeScreen
import com.example.transporttracker.ui.trips.TripsScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Home.route) },
                    label = { Text("Home") },
                    icon = {}
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Trips.route) },
                    label = { Text("Trips") },
                    icon = {}
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Analytics.route) },
                    label = { Text("Analytics") },
                    icon = {}
                )
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {

            composable(Screen.Home.route) {
                HomeRoute()
            }

            composable(Screen.Trips.route) {
                TripsScreen()
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen()
            }
        }
    }
}