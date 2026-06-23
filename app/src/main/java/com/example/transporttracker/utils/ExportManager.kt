package com.example.transporttracker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.transporttracker.data.repository.TransportRepository
import com.example.transporttracker.ui.trips.TripUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TransportRepository
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
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    suspend fun createGpxUri(trips: List<TripUiState>): Uri {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val file = File(context.cacheDir, "trips_export.gpx")
        file.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            writer.write("<gpx version=\"1.1\" creator=\"Transport Tracker\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
            for (trip in trips) {
                val points = repository.getPointsForTrip(trip.id)
                if (points.isEmpty()) continue
                writer.write("  <trk>\n")
                writer.write("    <name>${trip.date.xmlEscape()} – ${trip.transportType.xmlEscape()}</name>\n")
                writer.write("    <trkseg>\n")
                for (pt in points) {
                    writer.write("      <trkpt lat=\"${pt.latitude}\" lon=\"${pt.longitude}\">\n")
                    writer.write("        <time>${sdf.format(Date(pt.timestamp))}</time>\n")
                    if (pt.speed > 0f) writer.write("        <speed>${pt.speed}</speed>\n")
                    writer.write("      </trkpt>\n")
                }
                writer.write("    </trkseg>\n")
                writer.write("  </trk>\n")
            }
            writer.write("</gpx>")
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun String.csvSafe(): String =
        if (contains(',') || contains('"') || contains('\n') || contains('→')) {
            "\"${replace("\"", "\"\"")}\""
        } else this

    private fun String.xmlEscape(): String = replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
