package com.example.zmeygorynych

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.launch

class PositionCodeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PositionCodeAdapter
    private lateinit var etShortCode: EditText
    private lateinit var etFullTitle: EditText
    private lateinit var etCategory: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnClear: Button
    private lateinit var progressBar: ProgressBar

    private val viewModel: PositionCodeViewModel by lazy {
        val database = AppDatabase.getDatabase(this)
        val repository = PositionCodeRepository(database.positionCodeDao())
        PositionCodeViewModel(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_position_codes)

        // Настройка ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Коды должностей"

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        observeData()

        // Инициализируем дефолтные значения при первом запуске
        viewModel.initializeDefaults()
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rvPositionCodes)
        etShortCode = findViewById(R.id.etShortCode)
        etFullTitle = findViewById(R.id.etFullTitle)
        etCategory = findViewById(R.id.etCategory)
        btnAdd = findViewById(R.id.btnAddPositionCode)
        btnClear = findViewById(R.id.btnClearPositionCode)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        adapter = PositionCodeAdapter(
            onEditClick = { positionCode -> showEditDialog(positionCode) },
            onDeleteClick = { positionCode -> showDeleteDialog(positionCode) }
        )
        // Используем StaggeredGridLayoutManager для интеллектуального размещения в 3 колонки
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        btnAdd.setOnClickListener { addPositionCode() }
        btnClear.setOnClickListener { clearFields() }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.positionCodes.collect { positionCodes ->
                adapter.submitList(positionCodes)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun addPositionCode() {
        val shortCode = etShortCode.text.toString().trim()
        val fullTitle = etFullTitle.text.toString().trim()
        val category = etCategory.text.toString().trim()

        if (shortCode.isEmpty() || fullTitle.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем уникальность сокращения
        val existingCode = viewModel.getPositionCodeByShortCode(shortCode)
        if (existingCode != null) {
            Toast.makeText(this, "Сокращение '$shortCode' уже существует", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addPositionCode(shortCode, fullTitle, category)
        clearFields()
        Toast.makeText(this, "Код должности добавлен", Toast.LENGTH_SHORT).show()
    }

    private fun clearFields() {
        etShortCode.text.clear()
        etFullTitle.text.clear()
        etCategory.text.clear()
    }

    private fun showEditDialog(positionCode: PositionCode) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_position_code, null)
        val etDialogShortCode = dialogView.findViewById<EditText>(R.id.etDialogShortCode)
        val etDialogFullTitle = dialogView.findViewById<EditText>(R.id.etDialogFullTitle)
        val etDialogCategory = dialogView.findViewById<EditText>(R.id.etDialogCategory)

        etDialogShortCode.setText(positionCode.shortCode)
        etDialogFullTitle.setText(positionCode.fullTitle)
        etDialogCategory.setText(positionCode.category)

        AlertDialog.Builder(this)
            .setTitle("Редактировать код должности")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val newShortCode = etDialogShortCode.text.toString().trim()
                val newFullTitle = etDialogFullTitle.text.toString().trim()
                val newCategory = etDialogCategory.text.toString().trim()

                if (newShortCode.isNotEmpty() && newFullTitle.isNotEmpty() && newCategory.isNotEmpty()) {
                    // Проверяем уникальность, если сокращение изменилось
                    if (newShortCode != positionCode.shortCode) {
                        val existingCode = viewModel.getPositionCodeByShortCode(newShortCode)
                        if (existingCode != null) {
                            Toast.makeText(this, "Сокращение '$newShortCode' уже существует", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                    }

                    viewModel.updatePositionCode(positionCode, newShortCode, newFullTitle, newCategory)
                    Toast.makeText(this, "Код должности обновлен", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showDeleteDialog(positionCode: PositionCode) {
        AlertDialog.Builder(this)
            .setTitle("Удалить код должности")
            .setMessage("Вы уверены, что хотите удалить код '${positionCode.shortCode}'?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deletePositionCode(positionCode)
                Toast.makeText(this, "Код должности удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
