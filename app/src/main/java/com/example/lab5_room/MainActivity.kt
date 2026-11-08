package com.example.lab5_room

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var tvMode: TextView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnAdd: Button
    private lateinit var layoutEditButtons: LinearLayout
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnCancel: Button
    private lateinit var tvEmptyState: TextView
    private lateinit var recyclerView: RecyclerView

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NotesAdapter

    private var selectedNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMode = findViewById(R.id.tvMode)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnAdd = findViewById(R.id.btnAdd)
        layoutEditButtons = findViewById(R.id.layoutEditButtons)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        btnCancel = findViewById(R.id.btnCancel)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        recyclerView = findViewById(R.id.recyclerView)

        adapter = NotesAdapter(emptyList()) { note ->
            switchToEditMode(note)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Подписка на LiveData из ViewModel
        viewModel.allNotes.observe(this) { notes ->
            adapter.submitList(notes)
            if (notes.isEmpty()) {
                tvEmptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                tvEmptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        // Добавление новой заметки
        btnAdd.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Заполните заголовок заметки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            hideKeyboard()

            val newNote = Note(title = title, content = content)
            viewModel.insert(newNote)
            Toast.makeText(this, "Заметка сохранена в Room!", Toast.LENGTH_SHORT).show()
            switchToCreateMode()
        }

        // Обновление выбранной заметки
        btnUpdate.setOnClickListener {
            val current = selectedNote ?: return@setOnClickListener
            val title = etTitle.text.toString().trim()
            val content = etContent.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Заголовок не может быть пустым", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            hideKeyboard()

            val updatedNote = current.copy(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            viewModel.update(updatedNote)
            Toast.makeText(this, "Изменения сохранены в Room!", Toast.LENGTH_SHORT).show()
            switchToCreateMode()
        }

        // Удаление выбранной заметки
        btnDelete.setOnClickListener {
            val current = selectedNote ?: return@setOnClickListener

            hideKeyboard()

            viewModel.delete(current)
            Toast.makeText(this, "Заметка удалена из Room!", Toast.LENGTH_SHORT).show()
            switchToCreateMode()
        }

        // Отмена режима редактирования
        btnCancel.setOnClickListener {
            hideKeyboard()
            switchToCreateMode()
        }

        // Первичный режим
        switchToCreateMode()
    }

    private fun switchToCreateMode() {
        selectedNote = null
        adapter.setSelectedId(null)
        etTitle.text.clear()
        etContent.text.clear()

        tvMode.text = "➕ Создание новой заметки"
        tvMode.setTextColor(Color.parseColor("#00796B"))

        btnAdd.visibility = View.VISIBLE
        layoutEditButtons.visibility = View.GONE
    }

    private fun switchToEditMode(note: Note) {
        selectedNote = note
        adapter.setSelectedId(note.id)
        etTitle.setText(note.title)
        etContent.setText(note.content)

        tvMode.text = "✏️ Редактирование: ${note.title}"
        tvMode.setTextColor(Color.parseColor("#1976D2"))

        btnAdd.visibility = View.GONE
        layoutEditButtons.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val view = currentFocus ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
