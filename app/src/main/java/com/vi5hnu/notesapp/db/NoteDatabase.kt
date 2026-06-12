package com.vi5hnu.notesapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vi5hnu.notesapp.model.Task
import com.vi5hnu.notesapp.model.UserList

// v3: dropped the legacy Note entity. v4: added user-created lists. Destructive migration recreates.
@Database(entities = [Task::class, UserList::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDatabaseDao
    abstract fun userListDao(): UserListDao
}
