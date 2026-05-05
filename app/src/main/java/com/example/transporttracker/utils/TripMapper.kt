package com.example.transporttracker.utils

import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip

object TripMapper {

    fun map(
        entity: TripEntity
    ): Trip {

        return Trip(
            id = entity.id,
            startTime = entity.startTime,
            endTime = entity.endTime,
            transportType =
                runCatching {
                    TransportType.valueOf(
                        entity.transportType
                    )
                }.getOrDefault(
                    TransportType.UNKNOWN
                ),
            averageSpeed = entity.averageSpeed,
            dayType =
                runCatching {
                    DayType.valueOf(entity.dayType)
                }.getOrDefault(
                    DayType.WEEKDAY
                ),

            timeBin =
                runCatching {
                    TimeBin.valueOf(entity.timeBin)
                }.getOrDefault(
                    TimeBin.DAY
                )
        )
    }
}