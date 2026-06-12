package com.vi5hnu.notesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.vi5hnu.notesapp.ads.ConsentManager
import com.vi5hnu.notesapp.screen.AppScreen
import com.vi5hnu.notesapp.ui.theme.NotesAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Ads stay disabled until GDPR/EEA consent has been resolved.
    private val adsEnabled = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("tend_prefs", MODE_PRIVATE)

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
                    adsEnabled = adsEnabled.value
                )
            }
        }
    }
}
