package com.vi5hnu.notesapp.utils

import android.app.TimePickerDialog
import android.content.Context
import java.util.Locale

/**
 * Opens the platform time picker seeded with [current] (an "HH:mm" string, or null for a default
 * of 09:00) and invokes [onPicked] with the chosen "HH:mm" value.
 */
fun showTimePicker(context: Context, current: String?, onPicked: (String) -> Unit) {
    val parts = current?.split(":")?.mapNotNull { it.toIntOrNull() }
    val initH = parts?.getOrNull(0) ?: 9
    val initM = parts?.getOrNull(1) ?: 0
    TimePickerDialog(
        context,
        { _, h, m -> onPicked(String.format(Locale.US, "%02d:%02d", h, m)) },
        initH, initM, false
    ).show()
}
