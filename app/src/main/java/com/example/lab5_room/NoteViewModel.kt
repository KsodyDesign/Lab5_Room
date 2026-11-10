package com.example.lab5_room

import android.app.Application
import android.database.sqlite.SQLiteException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).noteDao()
    val allNotes: LiveData<List<Note>> = dao.getAllNotes()
    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean> = _busy
    private val _result = MutableLiveData<Pair<Boolean, String>?>(null)
    val result: LiveData<Pair<Boolean, String>?> = _result

    fun clearResult() { _result.value = null }
    private fun execute(message: String, operation: suspend () -> Boolean) {
        if (_busy.value == true) return
        _busy.value = true
        viewModelScope.launch {
            try {
                val success = operation()
                _result.value = success to if (success) message else "Запись не найдена или не сохранена"
            } catch (error: SQLiteException) {
                _result.value = false to "Ошибка базы данных: ${error.localizedMessage}"
            } finally {
                _busy.value = false
            }
        }
    }
    fun insert(note: Note) = execute("Заметка сохранена в Room") { dao.insert(note) != -1L }
    fun update(note: Note) = execute("Изменения сохранены в Room") { dao.update(note) > 0 }
    fun delete(note: Note) = execute("Заметка удалена из Room") { dao.delete(note) > 0 }
}
