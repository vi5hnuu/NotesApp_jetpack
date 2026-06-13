package com.vi5hnu.notesapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.vi5hnu.notesapp.MainActivity
import com.vi5hnu.notesapp.R
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.longDate
import com.vi5hnu.notesapp.utils.todayStr
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Home-screen widget listing today's + overdue tasks in a scrollable collection. Reads the
 * database via [EntryPointAccessors] and renders directly with [AppWidgetManager.updateAppWidget]
 * (no broadcast round-trip) so updates are reliable. Tapping a row opens that task; the header
 * opens the app and the + opens the new-task sheet.
 */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                renderAll(context, manager, appWidgetIds)
            } catch (_: Exception) {
                // Best-effort; the system retries on the next update period.
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val EXTRA_OPEN_ADD = "open_add"

        private fun widgetIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context)
                ?.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
                ?: IntArray(0)

        /** Refresh every placed widget — called from the ViewModel whenever tasks change. */
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = widgetIds(context)
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { renderAll(context, manager, ids) }
            }
        }

        private suspend fun renderAll(context: Context, manager: AppWidgetManager, ids: IntArray) {
            if (ids.isEmpty()) return
            val repo = EntryPointAccessors
                .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
                .taskRepository()
            val today = todayStr()
            val count = repo.getTasks().first()
                .count { !it.done && it.due != null && diffDays(it.due, today) <= 0 }
            val date = longDate()

            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_today)
                views.setTextViewText(R.id.widget_date, date)
                views.setTextViewText(R.id.widget_count, if (count == 0) "" else "$count to do")

                // Scrollable list backed by TodayWidgetService; unique data per id.
                val serviceIntent = Intent(context, TodayWidgetService::class.java).apply {
                    data = Uri.parse("noteswidget://$id")
                }
                views.setRemoteAdapter(R.id.widget_list, serviceIntent)
                views.setEmptyView(R.id.widget_list, R.id.widget_empty)

                // Tapping a row opens that task (fill-in intent set by the factory).
                val itemTemplate = PendingIntent.getActivity(
                    context, 1, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                views.setPendingIntentTemplate(R.id.widget_list, itemTemplate)

                views.setOnClickPendingIntent(R.id.widget_header, openApp(context, requestCode = 2))
                views.setOnClickPendingIntent(
                    R.id.widget_add,
                    openApp(context, requestCode = 3, openAdd = true)
                )

                manager.updateAppWidget(id, views)
            }
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list)
        }

        private fun openApp(context: Context, requestCode: Int, openAdd: Boolean = false): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .apply { if (openAdd) putExtra(EXTRA_OPEN_ADD, true) }
            return PendingIntent.getActivity(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
