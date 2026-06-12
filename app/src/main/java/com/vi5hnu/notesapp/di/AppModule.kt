package com.vi5hnu.notesapp.di

import android.content.Context
import androidx.room.Room
import com.vi5hnu.notesapp.db.NoteDatabase
import com.vi5hnu.notesapp.db.TaskDatabaseDao
import com.vi5hnu.notesapp.db.UserListDao
import com.vi5hnu.notesapp.repository.ListRepository
import com.vi5hnu.notesapp.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): NoteDatabase {
        return Room.databaseBuilder(context, NoteDatabase::class.java, "notesDB")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideTaskDao(db: NoteDatabase): TaskDatabaseDao = db.taskDao()

    @Singleton
    @Provides
    fun provideTaskRepository(dao: TaskDatabaseDao): TaskRepository = TaskRepository(dao)

    @Singleton
    @Provides
    fun provideUserListDao(db: NoteDatabase): UserListDao = db.userListDao()

    @Singleton
    @Provides
    fun provideListRepository(dao: UserListDao): ListRepository = ListRepository(dao)
}
