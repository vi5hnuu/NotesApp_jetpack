package com.vi5hnu.notesapp.db

import androidx.compose.runtime.MutableState
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vi5hnu.notesapp.model.Note
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface NoteDatabaseDao {
    @Query("Select * from notes")
    fun getNotes():Flow<List<Note>>

    @Query("Select * from notes where id =:id")
    suspend fun getNote(id:String):Note

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note:Note):Unit;

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(note:Note)

    @Query("Delete from notes")
    suspend fun deleteNotes():Unit

    @Query("Delete from notes where id =:id")
    suspend fun deleteNote(id:UUID):Unit
}
