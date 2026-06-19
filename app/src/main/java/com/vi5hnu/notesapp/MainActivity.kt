package com.vi5hnu.notesapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.vi5hnu.notesapp.ads.ConsentManager
import com.vi5hnu.notesapp.notifications.NotificationHelper
import com.vi5hnu.notesapp.screen.AppScreen
import com.vi5hnu.notesapp.widget.TodayWidgetProvider
import com.vi5hnu.notesapp.ui.theme.NotesAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Ads stay disabled until GDPR/EEA consent has been resolved.
    private val adsEnabled = mutableStateOf(false)

    // Task id from a tapped reminder notification; consumed by AppScreen to open that task.
    private val openTaskId = mutableStateOf<String?>(null)

    // Set when the widget's + button launches the app to add a task.
    private val openAdd = mutableStateOf(false)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* reminders no-op if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("tend_prefs", MODE_PRIVATE)

        requestNotificationPermissionIfNeeded()
        // Only handle launch extras on a genuine first creation — not on recreation/restore,
        // where the original intent is redelivered and would re-trigger the add sheet.
        if (savedInstanceState == null) consumeLaunchIntent(intent)

        // Gather consent, then initialize the ads SDK and enable ad surfaces.
        ConsentManager(this).gatherConsent(this) {
            (application as NoteApplication).initializeMobileAdsSdk()
            adsEnabled.value = true
        }

        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(prefs.getBoolean("dark_theme", systemDark)) }
            NotesAppTheme(darkTheme = darkTheme) {
                AppScreen(
                    darkTheme = darkTheme,
                    onThemeToggle = {
                        darkTheme = it
                        prefs.edit().putBoolean("dark_theme", it).apply()
                    },
                    adsEnabled = adsEnabled.value,
                    openTaskId = openTaskId.value,
                    onTaskOpened = { openTaskId.value = null },
                    openAdd = openAdd.value,
                    onAddOpened = { openAdd.value = false }
                )
            }
        }
    }

    // Tapped a reminder / widget while the app was already running.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeLaunchIntent(intent)
    }

    /**
     * Reads the one-shot launch extras (open-task / open-add) and **removes them** so they don't
     * fire again if the activity is recreated and re-reads its intent.
     *
     * Widget deep links carry a tap-time stamp ([TodayWidgetProvider.EXTRA_REQUEST_TS]). The system
     * re-delivers an activity's *base intent* on a cold relaunch (process death → reopen from
     * launcher/recents), which would otherwise re-trigger the add sheet on every launch. We honour a
     * stamped deep link only while it is fresh **and** not already consumed: [setIntent] cannot
     * rewrite the persisted base intent, so we also remember the last consumed stamp in prefs to
     * dedupe a fresh base intent that gets redelivered within the freshness window. Notification
     * intents carry no stamp and are always honoured. The cleaned intent is stored back via
     * [setIntent].
     */
    private fun consumeLaunchIntent(intent: Intent?) {
        intent ?: return
        val ts = intent.getLongExtra(TodayWidgetProvider.EXTRA_REQUEST_TS, 0L)
        val prefs = getSharedPreferences("tend_prefs", MODE_PRIVATE)
        val lastConsumed = prefs.getLong(KEY_LAST_DEEPLINK_TS, 0L)
        // A stamped link is stale if it timed out or was already consumed; unstamped (notification)
        // links have ts == 0 and are always honoured.
        val stale = ts != 0L &&
            (System.currentTimeMillis() - ts > FRESH_WINDOW_MS || ts <= lastConsumed)

        if (!stale) {
            intent.getStringExtra(NotificationHelper.EXTRA_OPEN_TASK_ID)?.let { openTaskId.value = it }
            if (intent.getBooleanExtra(TodayWidgetProvider.EXTRA_OPEN_ADD, false)) openAdd.value = true
            if (ts != 0L) prefs.edit().putLong(KEY_LAST_DEEPLINK_TS, ts).apply()
        }
        // Clear payload + stamp regardless, so a re-read of this intent never re-fires.
        intent.removeExtra(NotificationHelper.EXTRA_OPEN_TASK_ID)
        intent.removeExtra(TodayWidgetProvider.EXTRA_OPEN_ADD)
        intent.removeExtra(TodayWidgetProvider.EXTRA_REQUEST_TS)
        setIntent(intent)
    }

    companion object {
        /** A widget deep link is honoured only within this window of its tap time. */
        const val FRESH_WINDOW_MS = 30_000L
        /** Last consumed widget deep-link stamp — dedupes a redelivered, still-fresh base intent. */
        private const val KEY_LAST_DEEPLINK_TS = "last_deeplink_ts"
    }

    /** Ask for notification permission on Android 13+ so task reminders can be shown. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
