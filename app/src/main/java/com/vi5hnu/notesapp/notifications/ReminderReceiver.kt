package com.vi5hnu.notesapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Fired by AlarmManager when a task reminder or the daily nudge is due. Keeps the alarm payload
 * self-contained (title is passed in extras) so it does not need database access at fire time.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra(EXTRA_TYPE)) {
            TYPE_TASK -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: return
                val notificationId = intent.getIntExtra(EXTRA_NID, title.hashCode())
                NotificationHelper.showTaskReminder(
                    context, notificationId, title, intent.getStringExtra(EXTRA_SUB)
                )
            }
            TYPE_NUDGE -> NotificationHelper.showDailyNudge(context)
        }
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_TITLE = "title"
        const val EXTRA_SUB = "sub"
        const val EXTRA_NID = "nid"
        const val TYPE_TASK = "task"
        const val TYPE_NUDGE = "nudge"
    }
}
