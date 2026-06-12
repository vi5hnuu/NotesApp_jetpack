package com.vi5hnu.notesapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vi5hnu.notesapp.model.Task

// Version bumped to 3 after dropping the legacy Note entity — destructive migration recreates the schema.
@Database(entities = [Task::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDatabaseDao
}
