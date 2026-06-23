package com.example.transporttracker.ui.analytics

import com.example.transporttracker.domain.model.TransportType

data class AnalyticsUiState(
    val totalTrips: Int = 0,
    val totalDistanceKm: Float = 0f,
    val mostUsedTransport: String = "",
    val insights: List<AnalyticsInsight> = emptyList(),
    val transportShares: List<TransportShare> = emptyList(),
    val timeBinCounts: List<TimeBinCount> = emptyList(),
    val weekStats: PeriodStats = PeriodStats(),
    val monthStats: PeriodStats = PeriodStats()
)

data class PeriodStats(
    val tripCount: Int = 0,
    val distanceKm: Float = 0f,
    val durationMin: Long = 0L
)

data class AnalyticsInsight(
    val text: String,
    val count: Int
)

data class TransportShare(
    val transportType: TransportType,
    val tripCount: Int,
    val distanceKm: Float,
    val fraction: Float
)

data class TimeBinCount(
    val label: String,
    val count: Int,
    val fraction: Float
)
