package com.vi5hnu.notesapp.screen

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.vi5hnu.notesapp.data.DummyData
import com.vi5hnu.notesapp.model.Note
import java.util.UUID

class NoteViewModel:ViewModel() {
    private val _notes= mutableStateListOf<Note>()

    init {
        _notes.addAll(DummyData.getNotes());
    }
    fun getNotes():List<Note> {
        return this._notes.toList()
    }
    fun addNote(note:Note){this._notes.add(note)}
    fun removeNote(id:UUID){
        this._notes.removeIf{ it.id == id }
    }
}