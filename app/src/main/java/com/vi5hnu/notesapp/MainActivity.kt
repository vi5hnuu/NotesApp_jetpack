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
import com.vi5hnu.notesapp.ui.theme.NotesAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Ads stay disabled until GDPR/EEA consent has been resolved.
    private val adsEnabled = mutableStateOf(false)

    // Task id from a tapped reminder notification; consumed by AppScreen to open that task.
    private val openTaskId = mutableStateOf<String?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* reminders no-op if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("tend_prefs", MODE_PRIVATE)

        requestNotificationPermissionIfNeeded()
        openTaskId.value = intent?.getStringExtra(NotificationHelper.EXTRA_OPEN_TASK_ID)

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
                    onTaskOpened = { openTaskId.value = null }
                )
            }
        }
    }

    // Tapped a reminder while the app was already running.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(NotificationHelper.EXTRA_OPEN_TASK_ID)?.let { openTaskId.value = it }
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
