package com.example.transporttracker.ui.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.transporttracker.R
import com.example.transporttracker.ui.analytics.AnalyticsScreen
import com.example.transporttracker.ui.analytics.AnalyticsViewModel
import com.example.transporttracker.ui.home.HomeRoute
import com.example.transporttracker.ui.map.MapViewModel
import com.example.transporttracker.ui.map.TripMapScreen
import com.example.transporttracker.ui.trips.TripsScreen
import com.example.transporttracker.ui.trips.TripsViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val isTopLevel = currentRoute in listOf(
        Screen.Home.route,
        Screen.Trips.route,
        Screen.Analytics.route
    )

    Scaffold(
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = { navController.navigate(Screen.Home.route) },
                        label = { Text(stringResource(R.string.nav_home)) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Trips.route,
                        onClick = { navController.navigate(Screen.Trips.route) },
                        label = { Text(stringResource(R.string.nav_trips)) },
                        icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Analytics.route,
                        onClick = { navController.navigate(Screen.Analytics.route) },
                        label = { Text(stringResource(R.string.nav_analytics)) },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) }
                    )
                }
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
                val viewModel: TripsViewModel = hiltViewModel()
                val trips by viewModel.trips.collectAsStateWithLifecycle()
                val availableTypes by viewModel.availableTypes.collectAsStateWithLifecycle()
                val filterType by viewModel.filterType.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val chooserTitle = stringResource(R.string.export_chooser_title)

                LaunchedEffect("csv", viewModel) {
                    viewModel.exportEvent.collect { uri ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, chooserTitle))
                    }
                }

                LaunchedEffect("gpx", viewModel) {
                    viewModel.exportGpxEvent.collect { uri ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/gpx+xml"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, chooserTitle))
                    }
                }

                TripsScreen(
                    trips = trips,
                    availableFilters = availableTypes,
                    selectedFilter = filterType,
                    onFilterChange = { viewModel.setFilter(it) },
                    onTripClick = { tripId ->
                        navController.navigate(Screen.TripMap.createRoute(tripId))
                    },
                    onCorrectType = { tripId, type ->
                        viewModel.correctTripType(tripId, type)
                    },
                    onExportCsv = { viewModel.exportTrips() },
                    onExportGpx = { viewModel.exportGpx() }
                )
            }

            composable(Screen.Analytics.route) {
                val viewModel: AnalyticsViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                AnalyticsScreen(state = state)
            }

            composable(
                route = Screen.TripMap.route,
                arguments = listOf(navArgument("tripId") { type = NavType.LongType })
            ) {
                val viewModel: MapViewModel = hiltViewModel()
                val points by viewModel.points.collectAsStateWithLifecycle()
                val summary by viewModel.summary.collectAsStateWithLifecycle()

                TripMapScreen(
                    points = points,
                    summary = summary,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
