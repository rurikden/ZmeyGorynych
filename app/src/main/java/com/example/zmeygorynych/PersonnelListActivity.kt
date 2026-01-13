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
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnImport).setOnClickListener { selectImportFile() }
        findViewById<Button>(R.id.btnExport).setOnClickListener { selectExportFile() }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.personnelList.collect { personnelList ->
                adapter.submitList(personnelList)
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
                var isFirstLine = true
                var importedCount = 0

                while (reader.readLine().also { line = it } != null) {
                    if (isFirstLine) {
                        isFirstLine = false
                        continue
                    }

                    line?.let { csvLine ->
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
                            importedCount++
                        }
                    }
                }

                Toast.makeText(this, "Импортировано $importedCount записей", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка импорта: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToCsv(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = OutputStreamWriter(outputStream)

                writer.write("Фамилия,Имя,Отчество,Должность,Предприятие\n")

                viewModel.personnelList.value.forEach { personnel ->
                    val line = "\"${personnel.lastName}\",\"${personnel.firstName}\",\"${personnel.middleName}\",\"${personnel.position}\",\"${personnel.company}\"\n"
                    writer.write(line)
                }

                writer.close()
                Toast.makeText(this, "Экспорт завершен", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка экспорта: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
