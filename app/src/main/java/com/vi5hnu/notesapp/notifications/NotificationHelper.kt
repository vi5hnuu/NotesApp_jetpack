package com.vi5hnu.notesapp.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vi5hnu.notesapp.MainActivity
import com.vi5hnu.notesapp.R

/**
 * Builds notification channels and posts task reminders and the daily planning nudge.
 * All posting is guarded by [NotificationManagerCompat.areNotificationsEnabled] so it is a
 * no-op (never a crash) when the user hasn't granted the POST_NOTIFICATIONS permission.
 */
object NotificationHelper {

    const val CHANNEL_REMINDERS = "task_reminders"
    const val CHANNEL_NUDGE = "daily_plan"
    const val NUDGE_NOTIFICATION_ID = 1001

    /** Intent extra carrying the task id to open when a reminder notification is tapped. */
    const val EXTRA_OPEN_TASK_ID = "open_task_id"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDERS, "Task reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminders at a task's set time" }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_NUDGE, "Daily planning", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "A gentle nudge to plan your day" }
        )
    }

    fun showTaskReminder(context: Context, notificationId: Int, title: String, sub: String?, taskId: String?) {
        post(context, notificationId, CHANNEL_REMINDERS, title, sub ?: "Tap to open", taskId)
    }

    fun showDailyNudge(context: Context) {
        post(context, NUDGE_NOTIFICATION_ID, CHANNEL_NUDGE, "Plan your day", "What matters most today?", null)
    }

    private fun post(
        context: Context, id: Int, channel: String, title: String, text: String, openTaskId: String?
    ) {
        // Explicit POST_NOTIFICATIONS check (Android 13+) — no-op if not granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val launchIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .apply { if (openTaskId != null) putExtra(EXTRA_OPEN_TASK_ID, openTaskId) }
        // Unique request code per notification so each carries its own task-id extra.
        val contentIntent = PendingIntent.getActivity(
            context, id, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // areNotificationsEnabled() above is the permission guard; notify won't throw.
        runCatching { manager.notify(id, notification) }
    }
}
