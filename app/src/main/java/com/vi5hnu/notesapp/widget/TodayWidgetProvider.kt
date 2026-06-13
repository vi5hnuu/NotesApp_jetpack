package com.vi5hnu.notesapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.vi5hnu.notesapp.MainActivity
import com.vi5hnu.notesapp.R
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.repository.TaskRepository
import com.vi5hnu.notesapp.utils.diffDays
import com.vi5hnu.notesapp.utils.todayStr
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Home-screen widget showing today's + overdue tasks. Reads the database via [EntryPointAccessors]
 * (so no `@AndroidEntryPoint` bytecode transform), renders with [RemoteViews], and opens the app
 * when tapped. Refreshed periodically by the system and on task changes via [refresh].
 */
class TodayWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetDependencies {
        fun taskRepository(): TaskRepository
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = EntryPointAccessors
                    .fromApplication(context.applicationContext, WidgetDependencies::class.java)
                    .taskRepository()
                val today = todayStr()
                val tasks = repo.getTasks().first()
                    .filter { !it.done && it.due != null && diffDays(it.due, today) <= 0 }
                    .sortedWith(compareBy({ it.due }, { it.time ?: "99:99" }))
                appWidgetIds.forEach { id ->
                    manager.updateAppWidget(id, buildViews(context, tasks))
                }
            } catch (_: Exception) {
                // Best-effort; the system retries on the next update period.
            } finally {
                pending.finish()
            }
        }
    }

    private fun buildViews(context: Context, tasks: List<Task>): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_today)
        views.setTextViewText(R.id.widget_count, if (tasks.isEmpty()) "" else "${tasks.size} to do")

        val slots = intArrayOf(R.id.task_1, R.id.task_2, R.id.task_3, R.id.task_4, R.id.task_5)
        slots.forEachIndexed { i, slotId ->
            val task = tasks.getOrNull(i)
            if (task != null) {
                views.setTextViewText(slotId, "•  ${task.title}")
                views.setViewVisibility(slotId, View.VISIBLE)
            } else {
                views.setViewVisibility(slotId, View.GONE)
            }
        }

        views.setViewVisibility(R.id.widget_empty, if (tasks.isEmpty()) View.VISIBLE else View.GONE)
        if (tasks.size > slots.size) {
            views.setTextViewText(R.id.widget_more, "+${tasks.size - slots.size} more")
            views.setViewVisibility(R.id.widget_more, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_more, View.GONE)
        }

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openApp)
        return views
    }

    companion object {
        /** Ask the system to refresh every placed instance of this widget. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
            if (ids.isEmpty()) return
            context.sendBroadcast(
                Intent(context, TodayWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            )
        }
    }
}
