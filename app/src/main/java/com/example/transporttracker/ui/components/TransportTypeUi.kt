package com.example.transporttracker.ui.components

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Tram
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.transporttracker.R
import com.example.transporttracker.domain.model.TransportType

fun TransportType.icon(): ImageVector = when (this) {
    TransportType.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
    TransportType.BUS -> Icons.Default.DirectionsBus
    TransportType.TRAM -> Icons.Default.Tram
    TransportType.METRO -> Icons.Default.Subway
    TransportType.MCD -> Icons.Default.Train
    TransportType.UNKNOWN -> Icons.AutoMirrored.Filled.Help
}

fun TransportType.color(): Color = when (this) {
    TransportType.WALK -> Color(0xFF4CAF50)
    TransportType.BUS -> Color(0xFF2196F3)
    TransportType.TRAM -> Color(0xFFFF9800)
    TransportType.METRO -> Color(0xFFE53935)
    TransportType.MCD -> Color(0xFF9C27B0)
    TransportType.UNKNOWN -> Color(0xFF9E9E9E)
}

@StringRes
fun TransportType.nameResId(): Int = when (this) {
    TransportType.WALK -> R.string.transport_walk
    TransportType.BUS -> R.string.transport_bus
    TransportType.TRAM -> R.string.transport_tram
    TransportType.METRO -> R.string.transport_metro
    TransportType.MCD -> R.string.transport_mcd
    TransportType.UNKNOWN -> R.string.transport_unknown
}

fun TransportType.localizedName(context: Context): String = context.getString(nameResId())

@Composable
fun TransportType.localizedName(): String = stringResource(nameResId())
