package com.vi5hnu.notesapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.UserList

// v3: dropped legacy Note. v4: added user lists. v5: added Task.until. Destructive migration recreates.
@Database(entities = [Task::class, UserList::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDatabaseDao
    abstract fun userListDao(): UserListDao
}
