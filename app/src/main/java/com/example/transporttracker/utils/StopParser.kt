package com.example.transporttracker.utils

import android.content.Context
import com.example.transporttracker.domain.model.Stop
import org.json.JSONArray

object StopsParser {

    fun parse(
        context: Context
    ): List<Stop> {

        val json =
            context.assets
                .open("stops.json")
                .bufferedReader()
                .use { it.readText() }

        val array = JSONArray(json)

        val result =
            mutableListOf<Stop>()

        for (i in 0 until array.length()) {

            val item = array.getJSONObject(i)

            result.add(
                Stop(
                    stopId =
                        item.getLong("stop_id"),

                    stopName =
                        item.getString("stop_name"),

                    latitude =
                        item.getString("stop_lat")
                            .toDouble(),

                    longitude =
                        item.getString("stop_lon")
                            .toDouble(),

                    transportType =
                        item.getString("TransportType")
                )
            )
        }

        return result
    }
}