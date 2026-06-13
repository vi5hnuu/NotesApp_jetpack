package com.vi5hnu.notesapp.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Thin, injectable wrapper so the ViewModel can refresh the home-screen widget on task changes. */
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun update() {
        runCatching { TodayWidgetProvider.refresh(context) }
    }
}
