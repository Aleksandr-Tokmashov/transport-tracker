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

        val stops =
            mutableListOf<Stop>()

        for (i in 0 until array.length()) {

            val obj =
                array.getJSONObject(i)

            val geoData =
                obj.optJSONObject("geoData")
                    ?: continue

            val coordinates =
                geoData.optJSONArray("coordinates")
                    ?: continue

            if (coordinates.length() < 2) {
                continue
            }

            // В geojson:
            // [longitude, latitude]

            val longitude =
                coordinates.optDouble(0)

            val latitude =
                coordinates.optDouble(1)

            val stop = Stop(

                stopId =
                    obj.optLong("stop_id"),

                stopName =
                    obj.optString(
                        "stop_name",
                        "UNKNOWN"
                    ),

                latitude = latitude,

                longitude = longitude,

                transportType =
                    obj.optString(
                        "TransportType",
                        "UNKNOWN"
                    )
            )

            stops.add(stop)
        }

        return stops
    }
}