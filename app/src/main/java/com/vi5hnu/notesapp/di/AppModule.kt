package com.vi5hnu.notesapp.di

import android.content.Context
import androidx.room.Room
import com.vi5hnu.notesapp.db.NoteDatabase
import com.vi5hnu.notesapp.db.NoteDatabaseDao
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
    fun provideNotesDao(noteDatabase: NoteDatabase):NoteDatabaseDao{
        return noteDatabase.noteDao();
    }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context):NoteDatabase{
        return Room.databaseBuilder(context,NoteDatabase::class.java,"notesDB").fallbackToDestructiveMigration().build()
    }
}