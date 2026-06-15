package com.example.transporttracker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.transporttracker.ui.trips.TripUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createCsvUri(trips: List<TripUiState>): Uri {
        val file = File(context.cacheDir, "trips_export.csv")
        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("﻿") // BOM — корректное открытие в Excel
            writer.write("ID,Date,Start,End,Duration,Transport,Distance,Speed,Segments\n")
            trips.forEach { trip ->
                val row = listOf(
                    trip.id.toString(),
                    trip.date,
                    trip.startTime,
                    trip.endTime,
                    trip.duration,
                    trip.transportType,
                    trip.distance,
                    trip.averageSpeed,
                    trip.segments.joinToString(" → ")
                ).joinToString(",") { it.csvSafe() }
                writer.write(row + "\n")
            }
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    private fun String.csvSafe(): String =
        if (contains(',') || contains('"') || contains('\n') || contains('→')) {
            "\"${replace("\"", "\"\"")}\""
        } else this
}
