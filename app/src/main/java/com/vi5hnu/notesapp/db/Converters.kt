package com.vi5hnu.notesapp.db

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room TypeConverters for non-primitive types.
 * Note: Task.subtasks is stored as a raw JSON String — no converter needed for it.
 */
class Converters {
    @TypeConverter
    fun fromDate(value: Long): Date = Date(value)

    @TypeConverter
    fun toDate(date: Date): Long = date.time
}
