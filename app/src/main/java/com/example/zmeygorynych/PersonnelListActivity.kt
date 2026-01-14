package com.example.zmeygorynych

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class PersonnelListActivity : BaseActivity() {

    private lateinit var viewModel: PersonnelViewModel
    private lateinit var adapter: PersonnelAdapter
    private lateinit var etSearch: TextInputEditText

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                importFromCsv(uri)
            }
        }
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportToCsv(uri)
            }
        }
    }

    override fun getLayoutResourceId(): Int = R.layout.activity_personnel_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Настройка заголовка
        supportActionBar?.title = "Список персонала"

        // Инициализация ViewModel
        val database = AppDatabase.getDatabase(this)
        val repository = PersonnelRepository(database.personnelDao())
        val factory = PersonnelViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PersonnelViewModel::class.java]

        // Инициализация кэша кодов должностей
        initializePositionCodesCache(database)

        // Инициализация views
        initializeViews()
        setupRecyclerView()
        setupSearch()
        setupClickListeners()
        observeData()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        etSearch = findViewById(R.id.etSearch)
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.rvPersonnel)
        adapter = PersonnelAdapter(
            onEditClick = { personnel -> editPersonnel(personnel) },
            onDeleteClick = { personnel -> deletePersonnel(personnel) }
        )

        // Используем StaggeredGridLayoutManager для интеллектуального размещения в 3 колонки
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = staggeredGridLayoutManager
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                viewModel.searchPersonnel(query)
            }
        })
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnCreate).setOnClickListener { createNewPersonnel() }
        findViewById<Button>(R.id.btnImport).setOnClickListener { selectImportFile() }
        findViewById<Button>(R.id.btnExport).setOnClickListener { selectExportFile() }
        findViewById<Button>(R.id.btnPositionCodes).setOnClickListener { openPositionCodes() }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.personnelList.collect { personnelList ->
                adapter.submitList(personnelList)
            }
        }
    }

    private fun createNewPersonnel() {
        val intent = Intent(this, PersonnelActivity::class.java)
        startActivity(intent)
    }

    private fun openPositionCodes() {
        val intent = Intent(this, PositionCodeActivity::class.java)
        startActivity(intent)
    }

    private fun initializePositionCodesCache(database: AppDatabase) {
        lifecycleScope.launch {
            try {
                val positionCodes = database.positionCodeDao().getAllPositionCodes()
                Personnel.setPositionCodesCache(positionCodes)
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    private suspend fun getAllPositionCodesSync(): List<PositionCode> {
        val database = AppDatabase.getDatabase(this)
        return try {
            database.positionCodeDao().getAllPositionCodes()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun addPositionCodeIfNotExists(positionCode: PositionCode) {
        lifecycleScope.launch {
            try {
                val existingCode = getAllPositionCodesSync().find { it.shortCode == positionCode.shortCode }
                if (existingCode == null) {
                    val database = AppDatabase.getDatabase(this@PersonnelListActivity)
                    database.positionCodeDao().insertPositionCode(positionCode)
                    // Обновляем кэш
                    initializePositionCodesCache(database)
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    private fun editPersonnel(personnel: Personnel) {
        val intent = Intent(this, PersonnelActivity::class.java).apply {
            putExtra("edit_mode", true)
            putExtra("personnel_id", personnel.id)
            putExtra("last_name", personnel.lastName)
            putExtra("first_name", personnel.firstName)
            putExtra("middle_name", personnel.middleName)
            putExtra("position", personnel.position)
            putExtra("company", personnel.company)
        }
        startActivity(intent)
    }

    private fun deletePersonnel(personnel: Personnel) {
        AlertDialog.Builder(this)
            .setTitle("Удаление работника")
            .setMessage("Вы уверены, что хотите удалить ${personnel.fullName}?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deletePersonnel(personnel)
                Toast.makeText(this, "Работник удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun selectImportFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"
        }
        importLauncher.launch(intent)
    }

    private fun selectExportFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "personnel.csv")
        }
        exportLauncher.launch(intent)
    }

    private fun importFromCsv(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String?
                var currentSection = ""
                var personnelImported = 0
                var codesImported = 0

                while (reader.readLine().also { line = it } != null) {
                    line?.let { csvLine ->
                        val trimmedLine = csvLine.trim()

                        // Проверяем на разделитель секций
                        if (trimmedLine.startsWith("#")) {
                            currentSection = trimmedLine
                            // Пропускаем заголовок секции
                            reader.readLine() // Пропускаем строку с заголовками
                            return@let // Вместо continue используем return из lambda
                        }

                        // Пропускаем пустые строки
                        if (trimmedLine.isEmpty()) return@let

                        when (currentSection) {
                            "# Персонал" -> {
                                val parts = csvLine.split(",")
                                if (parts.size >= 5) {
                                    val personnel = Personnel(
                                        lastName = parts[0].trim().removeSurrounding("\""),
                                        firstName = parts[1].trim().removeSurrounding("\""),
                                        middleName = parts[2].trim().removeSurrounding("\""),
                                        position = parts[3].trim().removeSurrounding("\""),
                                        company = parts[4].trim().removeSurrounding("\"")
                                    )
                                    viewModel.addPersonnel(
                                        personnel.lastName,
                                        personnel.firstName,
                                        personnel.middleName,
                                        personnel.position,
                                        personnel.company
                                    )
                                    personnelImported++
                                }
                            }
                            "# Коды должностей" -> {
                                val parts = csvLine.split(",")
                                if (parts.size >= 3) {
                                    val positionCode = PositionCode(
                                        shortCode = parts[0].trim().removeSurrounding("\""),
                                        fullTitle = parts[1].trim().removeSurrounding("\""),
                                        category = parts[2].trim().removeSurrounding("\"")
                                    )
                                    addPositionCodeIfNotExists(positionCode)
                                    codesImported++
                                }
                            }
                        }
                    }
                }

                val message = "Импортировано: $personnelImported персонала, $codesImported кодов"
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка импорта: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToCsv(uri: Uri) {
        lifecycleScope.launch {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val writer = OutputStreamWriter(outputStream)

                    // Экспорт персонала
                    writer.write("# Персонал\n")
                    writer.write("Фамилия,Имя,Отчество,Должность,Предприятие\n")

                    viewModel.personnelList.value.forEach { personnel ->
                        val line = "\"${personnel.lastName}\",\"${personnel.firstName}\",\"${personnel.middleName}\",\"${personnel.position}\",\"${personnel.company}\"\n"
                        writer.write(line)
                    }

                    writer.write("\n")

                    // Экспорт кодов должностей
                    writer.write("# Коды должностей\n")
                    writer.write("Сокращение,Полное_название,Категория\n")

                    val positionCodes = getAllPositionCodesSync()
                    positionCodes.forEach { code ->
                        val line = "\"${code.shortCode}\",\"${code.fullTitle}\",\"${code.category}\"\n"
                        writer.write(line)
                    }

                    writer.close()
                    Toast.makeText(this@PersonnelListActivity, "Экспорт завершен", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PersonnelListActivity, "Ошибка экспорта: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
