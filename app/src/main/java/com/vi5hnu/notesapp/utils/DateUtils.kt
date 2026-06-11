package com.vi5hnu.notesapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val SDF = SimpleDateFormat("yyyy-MM-dd", Locale.US)

fun todayStr(): String = SDF.format(Date())

fun parseDate(s: String): Date = SDF.parse(s) ?: Date()

fun addDays(dateStr: String, n: Int): String {
    val cal = Calendar.getInstance().apply { time = parseDate(dateStr) }
    cal.add(Calendar.DAY_OF_YEAR, n)
    return SDF.format(cal.time)
}

/** Returns (a - b) in days: positive if a is after b */
fun diffDays(a: String, b: String): Int {
    val da = parseDate(a).time / 86_400_000L
    val db = parseDate(b).time / 86_400_000L
    return (da - db).toInt()
}

fun nextWeekend(): String {
    var d = Calendar.getInstance()
    do { d.add(Calendar.DAY_OF_YEAR, 1) } while (d.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
    return SDF.format(d.time)
}

fun nextOccurrence(from: String, recur: String): String? {
    return when (recur) {
        "daily" -> addDays(from, 1)
        "weekdays" -> {
            var next = addDays(from, 1)
            val cal = Calendar.getInstance().apply { time = parseDate(next) }
            while (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                next = addDays(next, 1)
                cal.time = parseDate(next)
            }
            next
        }
        "weekly" -> addDays(from, 7)
        "monthly" -> {
            val cal = Calendar.getInstance().apply { time = parseDate(from) }
            cal.add(Calendar.MONTH, 1)
            SDF.format(cal.time)
        }
        else -> null
    }
}

fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Good morning"
        h < 18 -> "Good afternoon"
        else   -> "Good evening"
    }
}

fun longDate(): String {
    return SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(Date())
}

fun dateLabel(dateStr: String): String {
    val today = todayStr()
    return when (diffDays(dateStr, today)) {
        0    -> "Today"
        1    -> "Tomorrow"
        -1   -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.ENGLISH).format(parseDate(dateStr))
    }
}

fun dayHeadLabel(dateStr: String): String {
    val today = todayStr()
    return when (diffDays(dateStr, today)) {
        0    -> "Today"
        -1   -> "Yesterday"
        else -> SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(parseDate(dateStr))
    }
}

fun timeLabel(timeStr: String): String {
    return try {
        val parts = timeStr.split(":")
        val h = parts[0].toInt()
        val m = parts[1].toInt()
        val suffix = if (h < 12) "AM" else "PM"
        val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        if (m == 0) "$h12 $suffix" else "$h12:${m.toString().padStart(2, '0')} $suffix"
    } catch (e: Exception) { timeStr }
}

fun recurLabel(recur: String): String = when (recur) {
    "daily"    -> "Daily"
    "weekdays" -> "Weekdays"
    "weekly"   -> "Weekly"
    "monthly"  -> "Monthly"
    else       -> recur
}

fun monthLabel(year: Int, month: Int): String {
    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    return SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time)
}
