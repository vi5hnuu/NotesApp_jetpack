package com.vi5hnu.notesapp.screen

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vi5hnu.notesapp.db.DummyData
import com.vi5hnu.notesapp.model.Note
import com.vi5hnu.notesapp.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(private val noteRepository: NoteRepository):ViewModel() {
    private val _notes= MutableStateFlow<List<Note>>(emptyList())
    val notes=_notes.asStateFlow()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository
                .getNotes()
                .distinctUntilChanged()
                .collect{
                    listOfNotes-> _notes.value=listOfNotes;
                }
        }
    }
    fun addNote(note:Note){
        viewModelScope.launch {
            noteRepository.addNote(note);
        }
    }
    fun update(note:Note){
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }
    fun removeNote(id:UUID){
        viewModelScope.launch {
            noteRepository.removeNote(id);
        }
    }
}