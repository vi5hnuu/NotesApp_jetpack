package com.vi5hnu.notesapp.data

import com.vi5hnu.notesapp.model.Note

class DummyData{
    companion object{
        private val _notes= listOf<Note>(
            Note(title = "A movie day", description = "As an illustration of class-level declarations, Factory design patterns including Static Factory Method and Abstract Factory are examples with a contextual connection to a class."),
           )
        fun getNotes():List<Note>{
            return _notes;
        }
    }
}