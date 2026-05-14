package com.example.transporttracker.utils

import android.content.Context
import com.example.transporttracker.domain.model.MetroEntrance
import org.json.JSONArray


object MetroParser {

    fun parse(context: Context): List<MetroEntrance> {

        val json =
            context.assets
                .open("metro.json")
                .bufferedReader()
                .use { it.readText() }

        val array = JSONArray(json)

        val result = mutableListOf<MetroEntrance>()

        for (i in 0 until array.length()) {

            val obj = array.getJSONObject(i)

            val station =
                obj.optString("NameOfStation")

            val line =
                obj.optString("Line")

            val geo =
                obj.getJSONObject("geoData")

            val coords =
                geo.getJSONArray("coordinates")

            val lon = coords.getDouble(0)
            val lat = coords.getDouble(1)

            result.add(
                MetroEntrance(
                    stationName = station,
                    line = line,
                    latitude = lat,
                    longitude = lon
                )
            )
        }

        return result
    }
}