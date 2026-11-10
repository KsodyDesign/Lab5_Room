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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : BaseActivity() {

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

    override fun onSaveInstanceState(outState: Bundle) {
        selectedNote?.id?.let { outState.putInt("selected_id", it) }
        outState.putString("edit_title", etTitle.text.toString())
        outState.putString("edit_content", etContent.text.toString())
        super.onSaveInstanceState(outState)
    }

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

        viewModel.busy.observe(this) { busy ->
            listOf(btnAdd, btnUpdate, btnDelete, btnCancel).forEach { it.isEnabled = !busy }
        }
        viewModel.result.observe(this) { result ->
            if (result != null) {
                Toast.makeText(this, result.second, Toast.LENGTH_LONG).show()
                if (result.first) switchToCreateMode()
                viewModel.clearResult()
            }
        }

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

        }

        // Удаление выбранной заметки
        btnDelete.setOnClickListener {
            val current = selectedNote ?: return@setOnClickListener

            hideKeyboard()

            viewModel.delete(current)

        }

        // Отмена режима редактирования
        btnCancel.setOnClickListener {
            hideKeyboard()
            switchToCreateMode()
        }

        // Первичный режим
        switchToCreateMode()
        if (savedInstanceState?.containsKey("selected_id") == true) {
            switchToEditMode(Note(
                id = savedInstanceState.getInt("selected_id"),
                title = savedInstanceState.getString("edit_title", ""),
                content = savedInstanceState.getString("edit_content", ""),
                timestamp = 0L
            ))
        }
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
