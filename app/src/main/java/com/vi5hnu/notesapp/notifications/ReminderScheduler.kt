package com.vi5hnu.notesapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.utils.parseDate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and cancels local reminder alarms via [AlarmManager].
 *
 * - Per-task reminders fire at the task's due date + time (inexact, Doze-friendly).
 * - A daily "plan your day" nudge fires at the user-configured time (default 08:00).
 *
 * [sync] reconciles the alarm set with the current task list and is safe to call on every task
 * change (alarms are replaced idempotently). All alarm operations are wrapped so a platform
 * failure can never crash the app. Channels/permissions are handled by [NotificationHelper].
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("tend_prefs", Context.MODE_PRIVATE)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scheduledTaskIds = mutableSetOf<String>()

    init {
        NotificationHelper.ensureChannels(context)
    }

    private fun remindersEnabled() = prefs.getBoolean("s_reminders", true)

    /** Reconcile per-task alarms with the current task list. Idempotent. */
    @Synchronized
    fun sync(tasks: List<Task>) {
        if (!remindersEnabled()) {
            cancelAllTasks()
            return
        }
        val eligible = tasks.filter { triggerFor(it) != null }
        val eligibleIds = eligible.mapTo(HashSet()) { it.id.toString() }
        // Cancel alarms for tasks that are no longer eligible (completed, deleted, time passed).
        (scheduledTaskIds - eligibleIds).toList().forEach { cancelTask(it) }
        // (Re)schedule eligible tasks.
        eligible.forEach { scheduleTask(it) }
    }

    @Synchronized
    fun cancelTask(taskId: String) {
        pendingIntentForTask(taskId, title = null, sub = null, create = false)?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        scheduledTaskIds.remove(taskId)
    }

    @Synchronized
    private fun cancelAllTasks() {
        scheduledTaskIds.toList().forEach { cancelTask(it) }
        scheduledTaskIds.clear()
    }

    private fun scheduleTask(task: Task) {
        val trigger = triggerFor(task) ?: return
        val pi = pendingIntentForTask(
            task.id.toString(), title = task.title, sub = task.notes.ifBlank { null }, create = true
        ) ?: return
        runCatching {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            scheduledTaskIds.add(task.id.toString())
        }
    }

    /** Returns the trigger time in millis if the task should have a future reminder, else null. */
    private fun triggerFor(task: Task): Long? {
        if (task.done || task.due == null || task.time == null) return null
        val millis = runCatching {
            val (h, m) = task.time.split(":").map { it.toInt() }
            Calendar.getInstance().apply {
                time = parseDate(task.due)
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.getOrNull() ?: return null
        return millis.takeIf { it > System.currentTimeMillis() }
    }

    private fun pendingIntentForTask(
        taskId: String, title: String?, sub: String?, create: Boolean
    ): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_TASK)
            putExtra(ReminderReceiver.EXTRA_NID, taskId.hashCode())
            putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
            if (title != null) putExtra(ReminderReceiver.EXTRA_TITLE, title)
            if (sub != null) putExtra(ReminderReceiver.EXTRA_SUB, sub)
        }
        val flags = if (create) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, taskId.hashCode(), intent, flags)
    }

    // ---- Daily planning nudge ----

    /** Reads the persisted nudge preference and (re)applies the daily alarm at the configured time. */
    fun syncDailyNudgeFromPrefs() = setDailyNudge(prefs.getBoolean("s_nudge", true))

    fun setDailyNudge(enabled: Boolean) {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_TYPE, ReminderReceiver.TYPE_NUDGE)
        if (!enabled) {
            PendingIntent.getBroadcast(
                context, NUDGE_REQUEST_CODE, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )?.let { alarmManager.cancel(it); it.cancel() }
            return
        }
        val pi = PendingIntent.getBroadcast(
            context, NUDGE_REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val (hour, minute) = nudgeTime()
        val nextTrigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        runCatching {
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, nextTrigger, AlarmManager.INTERVAL_DAY, pi
            )
        }
    }

    /** The configured nudge time ("HH:mm" pref), defaulting to 08:00. */
    private fun nudgeTime(): Pair<Int, Int> {
        val parts = (prefs.getString("s_nudge_time", "08:00") ?: "08:00")
            .split(":").mapNotNull { it.toIntOrNull() }
        return (parts.getOrNull(0) ?: 8) to (parts.getOrNull(1) ?: 0)
    }

    private companion object {
        const val NUDGE_REQUEST_CODE = 7000
    }
}
