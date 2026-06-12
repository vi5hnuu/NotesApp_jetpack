package com.vi5hnu.notesapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vi5hnu.notesapp.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Android clears all AlarmManager alarms on reboot. This receiver re-registers task reminders and
 * the daily nudge after the device restarts so reminders keep working without the app being opened.
 *
 * Uses [EntryPointAccessors] (rather than `@AndroidEntryPoint` field injection) to obtain
 * dependencies — this avoids the Hilt Gradle plugin's bytecode transform for receivers.
 */
class BootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootDependencies {
        fun taskRepository(): TaskRepository
        fun reminderScheduler(): ReminderScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val deps = EntryPointAccessors.fromApplication(
            context.applicationContext, BootDependencies::class.java
        )
        // Keep the receiver alive while we read the DB and re-arm alarms off the main thread.
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                deps.reminderScheduler().sync(deps.taskRepository().getTasks().first())
                deps.reminderScheduler().syncDailyNudgeFromPrefs()
            } catch (_: Exception) {
                // Best-effort; reminders also re-sync the next time the app is opened.
            } finally {
                pending.finish()
            }
        }
    }
}
