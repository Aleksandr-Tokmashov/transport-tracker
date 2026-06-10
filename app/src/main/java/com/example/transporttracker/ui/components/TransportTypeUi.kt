package com.example.transporttracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Subway
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Tram
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
