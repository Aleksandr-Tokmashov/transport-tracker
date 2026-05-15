package com.example.transporttracker.utils

import android.content.Context
import com.example.transporttracker.domain.model.McdEntrance
import org.json.JSONArray

object McdParser {

    fun parse(
        context: Context
    ): List<McdEntrance> {

        val json =
            context.assets
                .open("mcd.json")
                .bufferedReader()
                .use { it.readText() }

        val array = JSONArray(json)

        val result =
            mutableListOf<McdEntrance>()

        for (i in 0 until array.length()) {

            val obj =
                array.getJSONObject(i)

            val diameterArray =
                obj.getJSONArray("Diameter")

            if (diameterArray.length() == 0) {
                continue
            }

            val diameterObj =
                diameterArray.getJSONObject(0)

            val stationName =
                diameterObj.optString(
                    "StationName"
                )

            val diameterName =
                diameterObj.optString(
                    "DiameterName"
                )

            val geo =
                obj.getJSONObject("geoData")

            val coords =
                geo.getJSONArray("coordinates")

            val lon =
                coords.getDouble(0)

            val lat =
                coords.getDouble(1)

            result.add(
                McdEntrance(
                    stationName = stationName,
                    diameterName = diameterName,
                    latitude = lat,
                    longitude = lon
                )
            )
        }

        return result
    }
}