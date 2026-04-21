package com.example.transporttracker.utils

import com.example.transporttracker.data.local.entity.TripEntity
import com.example.transporttracker.domain.model.DayType
import com.example.transporttracker.domain.model.TimeBin
import com.example.transporttracker.domain.model.TransportType
import com.example.transporttracker.domain.model.Trip

fun TripEntity.toDomain(): Trip {

    return Trip(
        id = id,
        startTime = startTime,
        endTime = endTime,
        transportType = TransportType.valueOf(transportType),
        averageSpeed = averageSpeed,
        dayType = DayType.valueOf(dayType),
        timeBin = TimeBin.valueOf(timeBin)
    )
}