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
import com.vi5hnu.notesapp.notifications.NotificationHelper
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

    /**
     * Click trampoline + clock/date handling. Widget row/+ taps arrive here (not the activity
     * directly) so we can stamp the *tap time* on the launch intent: [MainActivity] only honours a
     * deep link whose stamp is fresh, which stops a re-delivered stale base intent from re-opening
     * the add sheet on later launches. On date/time/timezone changes we refresh so "today" rolls
     * over at midnight.
     */
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_OPEN_ADD -> startApp(context) { putExtra(EXTRA_OPEN_ADD, true) }
            ACTION_OPEN_TASK -> {
                val taskId = intent.getStringExtra(NotificationHelper.EXTRA_OPEN_TASK_ID)
                startApp(context) {
                    if (taskId != null) putExtra(NotificationHelper.EXTRA_OPEN_TASK_ID, taskId)
                }
            }
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> refreshAsync(context)
            else -> super.onReceive(context, intent)
        }
    }

    /**
     * Refresh from a broadcast we receive directly (date/clock changes). Uses [goAsync] so the
     * process stays alive until the render finishes — date changes can cold-start the process just
     * for this broadcast, and a detached coroutine would otherwise be killed mid-render.
     */
    private fun refreshAsync(context: Context) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = AppWidgetManager.getInstance(context) ?: return@launch
                renderAll(context, manager, widgetIds(context))
            } catch (_: Exception) {
                // Best-effort; the system retries on the next update period.
            } finally {
                pending.finish()
            }
        }
    }

    /** Launch the app at tap time with a freshness stamp the activity validates. */
    private fun startApp(context: Context, payload: Intent.() -> Unit) {
        val launch = Intent(context, MainActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            .putExtra(EXTRA_REQUEST_TS, System.currentTimeMillis())
            .apply(payload)
        runCatching { context.startActivity(launch) }
    }

    companion object {
        const val EXTRA_OPEN_ADD = "open_add"
        /** Tap-time stamp; the activity ignores deep links older than [MainActivity.FRESH_WINDOW_MS]. */
        const val EXTRA_REQUEST_TS = "request_ts"

        private const val ACTION_OPEN_ADD = "com.vi5hnu.notesapp.widget.OPEN_ADD"
        private const val ACTION_OPEN_TASK = "com.vi5hnu.notesapp.widget.OPEN_TASK"

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
                .count { !it.done && (it.due == null || diffDays(it.due, today) <= 0) }
            val date = longDate()

            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_today)
                views.setTextViewText(R.id.widget_date, date)
                views.setTextViewText(R.id.widget_count, if (count == 0) "" else "$count to do")

                // Scrollable list backed by TodayWidgetService. The data Uri changes every render
                // so setRemoteAdapter forces the host to rebind the factory and call
                // onDataSetChanged once with fresh DB data — otherwise an unchanged Uri can be
                // skipped and the list shows stale rows (e.g. an "Overdue" tag after rescheduling).
                // This rebind is the sole refresh trigger; no extra notifyAppWidgetViewDataChanged
                // is needed (which would re-run onDataSetChanged and read the DB a second time).
                val serviceIntent = Intent(context, TodayWidgetService::class.java).apply {
                    data = Uri.parse("noteswidget://$id/${System.currentTimeMillis()}")
                }
                views.setRemoteAdapter(R.id.widget_list, serviceIntent)
                views.setEmptyView(R.id.widget_list, R.id.widget_empty)

                // Tapping a row opens that task; the factory supplies EXTRA_OPEN_TASK_ID as the
                // fill-in, which the trampoline reads in onReceive and stamps with the tap time.
                views.setPendingIntentTemplate(R.id.widget_list, trampoline(context, ACTION_OPEN_TASK))

                views.setOnClickPendingIntent(R.id.widget_header, openApp(context, requestCode = 2))
                views.setOnClickPendingIntent(
                    R.id.widget_add, trampoline(context, ACTION_OPEN_ADD)
                )

                manager.updateAppWidget(id, views)
            }
        }

        /** Broadcast PendingIntent back to this provider so the click is handled at tap time. */
        private fun trampoline(context: Context, action: String): PendingIntent {
            val intent = Intent(context, TodayWidgetProvider::class.java).setAction(action)
            // MUTABLE so the row template can merge its per-task fill-in (the task id).
            return PendingIntent.getBroadcast(
                context, action.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        }

        /** Plain "open the app" with no deep link (header tap). */
        private fun openApp(context: Context, requestCode: Int): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return PendingIntent.getActivity(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
