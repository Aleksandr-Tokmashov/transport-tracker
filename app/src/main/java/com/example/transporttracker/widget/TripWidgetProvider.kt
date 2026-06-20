package com.example.transporttracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.transporttracker.MainActivity
import com.example.transporttracker.R
import com.example.transporttracker.utils.Constants

class TripWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, TripWidgetProvider::class.java)
            )
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val prefs = context.getSharedPreferences(Constants.WIDGET_PREFS, Context.MODE_PRIVATE)
            val typeName = prefs.getString(Constants.WIDGET_KEY_TYPE, null)
            val distanceM = prefs.getFloat(Constants.WIDGET_KEY_DISTANCE, 0f)
            val durationMs = prefs.getLong(Constants.WIDGET_KEY_DURATION, 0L)

            val views = RemoteViews(context.packageName, R.layout.widget_trip)

            if (typeName != null) {
                views.setTextViewText(R.id.widget_type, typeName)
                val km = distanceM / 1000f
                val minutes = (durationMs / 60_000L).toInt()
                views.setTextViewText(
                    R.id.widget_details,
                    context.getString(R.string.widget_trip_details, km, minutes)
                )
            } else {
                views.setTextViewText(R.id.widget_type, context.getString(R.string.widget_no_trips))
                views.setTextViewText(R.id.widget_details, "")
            }

            val tapIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, tapIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
