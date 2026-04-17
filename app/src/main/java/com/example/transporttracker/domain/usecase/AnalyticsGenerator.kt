package com.example.transporttracker.domain.usecase

import com.example.transporttracker.domain.model.AnalyticsPattern
import com.example.transporttracker.domain.model.Trip

class AnalyticsGenerator {

    fun generatePatterns(
        trips: List<Trip>
    ): List<AnalyticsPattern> {

        val grouped =
            trips.groupBy {
                Triple(
                    it.transportType,
                    it.dayType,
                    it.timeBin
                )
            }

        val patterns =
            mutableListOf<AnalyticsPattern>()

        grouped.forEach { (key, tripsList) ->

            if (tripsList.size >= 3) {

                val text =
                    buildPatternText(
                        transport = key.first.name,
                        dayType = key.second.name,
                        timeBin = key.third.name
                    )

                patterns.add(
                    AnalyticsPattern(
                        text = text,
                        count = tripsList.size
                    )
                )
            }
        }

        return patterns
    }

    private fun buildPatternText(
        transport: String,
        dayType: String,
        timeBin: String
    ): String {

        val dayText =
            if (dayType == "WEEKDAY") {
                "В будние дни"
            } else {
                "В выходные"
            }

        val timeText =
            when (timeBin) {

                "MORNING" -> "утром"

                "DAY" -> "днем"

                "EVENING" -> "вечером"

                else -> "ночью"
            }

        val transportText =
            when (transport) {

                "BUS" -> "BUS"

                "METRO" -> "METRO"

                "TRAM" -> "TRAM"

                else -> "UNKNOWN"
            }

        return "$dayText $timeText вы чаще используете $transportText"
    }
}