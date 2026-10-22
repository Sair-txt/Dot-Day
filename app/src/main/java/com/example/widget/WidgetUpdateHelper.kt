package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Result of a widget refresh operation providing truthful user diagnostics.
 */
sealed class WidgetRefreshResult {
    data class Success(val widgetCount: Int, val message: String) : WidgetRefreshResult()
    data class NoWidgets(val message: String) : WidgetRefreshResult()
    data class Failure(val error: String) : WidgetRefreshResult()
}

object WidgetUpdateHelper {
    private const val TAG = "DotDayWidgetUpdate"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Executes a comprehensive, verified refresh of all active Glance and system AppWidget instances.
     * Guaranteed to query active widgets, re-render Glance content with current preferences,
     * and broadcast system widget updates.
     */
    suspend fun refreshWidgets(context: Context): WidgetRefreshResult = withContext(Dispatchers.IO) {
        try {
            val appContext = context.applicationContext

            // 1. Query Android OS AppWidgetManager for placed widget instances
            val appWidgetManager = AppWidgetManager.getInstance(appContext)
            val componentName = ComponentName(appContext, YearInDotsWidgetReceiver::class.java)
            val appWidgetIds = try {
                appWidgetManager.getAppWidgetIds(componentName)
            } catch (e: Exception) {
                Log.w(TAG, "Failed querying appWidgetIds from AppWidgetManager", e)
                intArrayOf()
            }

            // 2. Query Glance manager for registered GlanceIds
            val glanceManager = GlanceAppWidgetManager(appContext)
            val glanceIds = try {
                glanceManager.getGlanceIds(YearInDotsGlanceWidget::class.java)
            } catch (e: Exception) {
                Log.w(TAG, "Failed querying glanceIds from GlanceAppWidgetManager", e)
                emptyList()
            }

            Log.d(TAG, "Found ${appWidgetIds.size} appWidgetIds and ${glanceIds.size} glanceIds")

            // 3. Trigger GlanceAppWidget updateAll to recompute with newest settings
            val widget = YearInDotsGlanceWidget()
            try {
                widget.updateAll(appContext)
            } catch (e: Exception) {
                Log.e(TAG, "Error during YearInDotsGlanceWidget.updateAll", e)
            }

            // 4. Update each specific Glance instance explicitly to ensure fresh composition
            for (id in glanceIds) {
                try {
                    widget.update(appContext, id)
                } catch (e: Exception) {
                    Log.w(TAG, "Error updating specific glanceId: $id", e)
                }
            }

            // 5. Broadcast system ACTION_APPWIDGET_UPDATE so OS widget host wakes up and syncs
            if (appWidgetIds.isNotEmpty()) {
                val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    component = componentName
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                appContext.sendBroadcast(updateIntent)
                try {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, android.R.id.background)
                } catch (e: Exception) {
                    // Ignore non-collection view data change exception
                }
            }

            val totalActive = maxOf(appWidgetIds.size, glanceIds.size)
            if (totalActive > 0) {
                WidgetRefreshResult.Success(
                    widgetCount = totalActive,
                    message = if (totalActive == 1) "Refreshed 1 home-screen widget" else "Refreshed $totalActive home-screen widgets"
                )
            } else {
                WidgetRefreshResult.NoWidgets(
                    message = "No widget placed on home screen. Long-press home screen to add Dot Day."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during widget refresh", e)
            WidgetRefreshResult.Failure(e.localizedMessage ?: "Unknown refresh error")
        }
    }

    /**
     * Asynchronous fire-and-forget update invoked after preferences or events change.
     */
    fun updateAllWidgets(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                refreshWidgets(appContext)
            } catch (e: Exception) {
                Log.w(TAG, "Background update failed", e)
            }
        }
    }
}
