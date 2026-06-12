package com.vi5hnu.notesapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vi5hnu.notesapp.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Android clears all AlarmManager alarms on reboot. This receiver re-registers task reminders and
 * the daily nudge after the device restarts so reminders keep working without the app being opened.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepo: TaskRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        // Keep the receiver alive while we read the DB and re-arm alarms off the main thread.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduler.sync(taskRepo.getTasks().first())
                scheduler.syncDailyNudgeFromPrefs()
            } catch (_: Exception) {
                // Best-effort; reminders also re-sync the next time the app is opened.
            } finally {
                pending.finish()
            }
        }
    }
}
