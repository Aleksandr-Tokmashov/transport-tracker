package com.example.transporttracker.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.transporttracker.R
import com.example.transporttracker.data.local.entity.GpsPointEntity
import com.example.transporttracker.ui.components.color
import com.example.transporttracker.ui.components.icon
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMapScreen(
    points: List<GpsPointEntity>,
    summary: TripSummaryState?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.map_route_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                )
                if (summary != null) {
                    TripStatsBar(summary = summary)
                }
            }
        }
    ) { padding ->

        if (points.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_gps_data))
            }
        } else {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { mapView },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                update = { map -> drawTrack(map, points) }
            )
        }
    }
}

@Composable
private fun TripStatsBar(summary: TripSummaryState) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = summary.transportType.icon(),
                contentDescription = null,
                tint = summary.transportType.color(),
                modifier = Modifier.size(22.dp)
            )

            StatItem(
                label = summary.transportType.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                bold = true
            )

            if (summary.avgSpeedKmh > 0f) {
                StatItem(label = "${summary.avgSpeedKmh.toInt()} км/ч")
            }

            if (summary.distanceKm > 0f) {
                StatItem(label = "%.1f км".format(summary.distanceKm))
            }

            StatItem(label = "${summary.durationMin} мин")
        }
    }
}

@Composable
private fun StatItem(label: String, bold: Boolean = false) {
    Text(
        text = label,
        style = if (bold) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        else MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

private fun drawTrack(mapView: MapView, points: List<GpsPointEntity>) {
    val geoPoints = points.map { GeoPoint(it.latitude, it.longitude) }

    mapView.overlays.clear()
    mapView.overlays.add(Polyline().apply { setPoints(geoPoints) })

    val midLat = geoPoints.map { it.latitude }.average()
    val midLon = geoPoints.map { it.longitude }.average()
    mapView.controller.setZoom(15.0)
    mapView.controller.setCenter(GeoPoint(midLat, midLon))
    mapView.invalidate()
}
