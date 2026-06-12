package com.example.transporttracker.domain.usecase

import com.example.transporttracker.domain.model.AnalyticsPattern
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip

class AnalyticsGenerator {

    fun generatePatterns(trips: List<Trip>): List<AnalyticsPattern> {

        val grouped = trips.groupBy { Triple(it.transportType, it.dayType, it.timeBin) }
        val patterns = mutableListOf<AnalyticsPattern>()

        grouped.forEach { (key, tripsList) ->
            if (tripsList.size >= 3) {
                patterns.add(
                    AnalyticsPattern(
                        text = buildPatternText(
                            transport = key.first.name,
                            dayType = key.second.name,
                            timeBin = key.third.name
                        ),
                        count = tripsList.size
                    )
                )
            }
        }

        return patterns
    }

    private fun buildPatternText(transport: String, dayType: String, timeBin: String): String {

        val dayText = if (dayType == "WEEKDAY") "В будние дни" else "В выходные"

        val timeText = when (timeBin) {
            "MORNING" -> "утром"
            "DAY" -> "днём"
            "EVENING" -> "вечером"
            else -> "ночью"
        }

        val transportText = when (transport) {
            "BUS" -> "ездите на автобусе"
            "METRO" -> "ездите на метро"
            "TRAM" -> "ездите на трамвае"
            "MCD" -> "ездите на МЦД"
            "WALK" -> "ходите пешком"
            else -> "используете транспорт"
        }

        return "$dayText $timeText вы чаще $transportText"
    }

    fun getMostUsedTransport(trips: List<Trip>): String {
        val type = trips.groupBy { it.transportType }
            .maxByOrNull { it.value.size }?.key
            ?: return "Нет данных"
        return when (type) {
            TransportType.BUS -> "Автобус"
            TransportType.METRO -> "Метро"
            TransportType.TRAM -> "Трамвай"
            TransportType.MCD -> "МЦД"
            TransportType.WALK -> "Пешком"
            TransportType.UNKNOWN -> "Неизвестно"
        }
    }
}
