package com.pakminseok.managefridge

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import android.widget.RemoteViews
import android.content.ComponentName



/**
 * Implementation of App Widget functionality.
 */
const val WIDGET_SYNC = "WIDGET_SYNC"

class OverviewWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(WIDGET_SYNC == intent.action){
            val appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, OverviewWidget::class.java))
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
        }
        super.onReceive(context, intent)
    }

    companion object {

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dbHandler = DBHandler(context)
            val today = SimpleDateFormat("yyyy-MM-dd").format(Date().time)
            val cntOfToday = dbHandler.getFridgeCnt(today)
            // Construct the RemoteViews object

            val intent = Intent(context, OverviewWidget::class.java)
            intent.action = WIDGET_SYNC
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val views = RemoteViews(context.packageName, R.layout.overview_widget)
            views.setTextViewText(R.id.appwidget_result, cntOfToday.toString()+"건")
            views.setOnClickPendingIntent(R.id.iv_refresh, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            dbHandler.close()
        }
    }
}