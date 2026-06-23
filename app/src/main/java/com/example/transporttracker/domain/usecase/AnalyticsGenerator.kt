package com.example.transporttracker.domain.usecase

import android.content.Context
import com.example.transporttracker.R
import com.example.transporttracker.domain.model.AnalyticsPattern
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip
import com.example.transporttracker.ui.analytics.PeriodStats
import com.example.transporttracker.ui.analytics.TimeBinCount
import com.example.transporttracker.ui.analytics.TransportShare
import com.example.transporttracker.ui.components.localizedName
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generatePatterns(trips: List<Trip>): List<AnalyticsPattern> {
        val grouped = trips.groupBy { Triple(it.transportType, it.dayType, it.timeBin) }
        return grouped.mapNotNull { (key, tripsList) ->
            if (tripsList.size >= 3) {
                AnalyticsPattern(
                    text = buildPatternText(key.first, key.second.name, key.third.name),
                    count = tripsList.size
                )
            } else null
        }
    }

    private fun buildPatternText(transport: TransportType, dayType: String, timeBin: String): String {
        val dayText = if (dayType == "WEEKDAY")
            context.getString(R.string.pattern_day_weekday)
        else
            context.getString(R.string.pattern_day_weekend)

        val timeText = when (timeBin) {
            "MORNING" -> context.getString(R.string.pattern_time_morning)
            "DAY" -> context.getString(R.string.pattern_time_day)
            "EVENING" -> context.getString(R.string.pattern_time_evening)
            else -> context.getString(R.string.pattern_time_night)
        }

        val transportText = when (transport) {
            TransportType.BUS -> context.getString(R.string.pattern_transport_bus)
            TransportType.METRO -> context.getString(R.string.pattern_transport_metro)
            TransportType.TRAM -> context.getString(R.string.pattern_transport_tram)
            TransportType.MCD -> context.getString(R.string.pattern_transport_mcd)
            TransportType.WALK -> context.getString(R.string.pattern_transport_walk)
            else -> context.getString(R.string.pattern_transport_other)
        }

        return context.getString(R.string.pattern_format, dayText, timeText, transportText)
    }

    fun getMostUsedTransport(trips: List<Trip>): String {
        val type = trips.groupBy { it.transportType }
            .maxByOrNull { it.value.size }?.key
            ?: return context.getString(R.string.no_data)
        return type.localizedName(context)
    }

    fun getTotalDistanceKm(trips: List<Trip>): Float =
        trips.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f

    fun getPeriodStats(trips: List<Trip>, cutoffMs: Long): PeriodStats {
        val filtered = trips.filter { it.startTime >= cutoffMs }
        return PeriodStats(
            tripCount = filtered.size,
            distanceKm = filtered.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f,
            durationMin = filtered.sumOf { it.endTime - it.startTime } / 60_000L
        )
    }

    fun getTransportShares(trips: List<Trip>): List<TransportShare> {
        val byType = trips
            .filter { it.transportType != TransportType.UNKNOWN }
            .groupBy { it.transportType }
        if (byType.isEmpty()) return emptyList()
        val maxCount = byType.values.maxOf { it.size }
        return byType.entries
            .sortedByDescending { it.value.size }
            .map { (type, list) ->
                TransportShare(
                    transportType = type,
                    tripCount = list.size,
                    distanceKm = list.sumOf { it.distanceMeters.toDouble() }.toFloat() / 1000f,
                    fraction = list.size.toFloat() / maxCount
                )
            }
    }

    fun getTimeBinCounts(trips: List<Trip>): List<TimeBinCount> {
        val order = listOf(
            "MORNING" to context.getString(R.string.chart_time_morning),
            "DAY" to context.getString(R.string.chart_time_day),
            "EVENING" to context.getString(R.string.chart_time_evening),
            "NIGHT" to context.getString(R.string.chart_time_night)
        )
        val counts = trips.groupBy { it.timeBin.name }.mapValues { it.value.size }
        val max = (counts.values.maxOrNull() ?: 0).coerceAtLeast(1)
        return order.map { (bin, label) ->
            val count = counts[bin] ?: 0
            TimeBinCount(label = label, count = count, fraction = count.toFloat() / max)
        }
    }
}
