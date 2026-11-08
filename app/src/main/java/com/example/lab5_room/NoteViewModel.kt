package com.example.lab5_room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: NoteDao = AppDatabase.getDatabase(application).noteDao()

    val allNotes: LiveData<List<Note>> = dao.getAllNotes()

    fun insert(note: Note) = viewModelScope.launch {
        dao.insert(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        dao.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        dao.delete(note)
    }
}
