package com.example.lab5_room

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnClear: Button
    private lateinit var recyclerView: RecyclerView

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

    private var selectedNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnAdd = findViewById(R.id.btnAdd)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnClear = findViewById(R.id.btnClear)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = NotesAdapter(emptyList()) { note ->
            selectedNote = note
            etTitle.setText(note.title)
            etContent.setText(note.content)
            Toast.makeText(this, "Выбрана заметка: ${note.title}", Toast.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Подписка на LiveData из ViewModel
        viewModel.allNotes.observe(this) { notes ->
            adapter.submitList(notes)
        }

        // Добавление новой заметки
        btnAdd.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Заголовок не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newNote = Note(title = title, content = content)
            viewModel.insert(newNote)
            Toast.makeText(this, "Заметка сохранена в Room", Toast.LENGTH_SHORT).show()
            clearInputs()
        }

        // Обновление выбранной заметки
        btnUpdate.setOnClickListener {
            val current = selectedNote
            if (current == null) {
                Toast.makeText(this, "Выберите заметку из списка для обновления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Заголовок не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedNote = current.copy(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            viewModel.update(updatedNote)
            Toast.makeText(this, "Заметка обновлена в Room", Toast.LENGTH_SHORT).show()
            clearInputs()
        }

        // Удаление выбранной заметки
        btnDelete.setOnClickListener {
            val current = selectedNote
            if (current == null) {
                Toast.makeText(this, "Выберите заметку из списка для удаления", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.delete(current)
            Toast.makeText(this, "Заметка удалена из Room", Toast.LENGTH_SHORT).show()
            clearInputs()
        }

        // Сброс полей ввода
        btnClear.setOnClickListener {
            clearInputs()
        }
    }

    private fun clearInputs() {
        selectedNote = null
        etTitle.text.clear()
        etContent.text.clear()
    }
}
