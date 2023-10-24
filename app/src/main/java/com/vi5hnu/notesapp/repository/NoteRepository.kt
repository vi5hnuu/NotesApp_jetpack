package com.vi5hnu.notesapp.repository

import com.vi5hnu.notesapp.db.NoteDatabaseDao
import com.vi5hnu.notesapp.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class NoteRepository @Inject constructor(private val notesDatabaseDao:NoteDatabaseDao) {
    suspend fun getNotes():Flow<List<Note>>{
        return notesDatabaseDao.getNotes().flowOn(Dispatchers.IO).conflate();
    }
    suspend fun addNote(note:Note){
        notesDatabaseDao.insertNote(note);
    }
    suspend fun updateNote(note:Note){
        notesDatabaseDao.update(note);
    }
    suspend fun removeNote(id:UUID){
        notesDatabaseDao.deleteNote(id);
    }
    suspend fun deleteNotes(){
        notesDatabaseDao.deleteNotes();
    }
}