package com.vi5hnu.notesapp.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(value: Long): Date {
        return Date(value);
    }

    @TypeConverter
    fun toDate(date: Date): Long {
        return date.time
    }
}