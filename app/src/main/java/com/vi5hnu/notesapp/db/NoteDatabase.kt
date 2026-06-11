package com.vi5hnu.notesapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vi5hnu.notesapp.model.Note
import com.vi5hnu.notesapp.model.Task

@Database(entities = [Note::class, Task::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDatabaseDao
    abstract fun taskDao(): TaskDatabaseDao
}
