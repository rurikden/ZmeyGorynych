package com.example.zmeygorynych

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

class PersonnelActivity : BaseActivity() {

    private lateinit var viewModel: PersonnelViewModel
    private lateinit var etLastName: TextInputEditText
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etMiddleName: TextInputEditText
    private lateinit var etPosition: TextInputEditText
    private lateinit var etCompany: TextInputEditText
    private lateinit var btnAdd: Button
    private lateinit var btnDelete: Button
    private var lastClickTime: Long = 0
    private var isEditMode = false
    private var editingPersonnelId: Long = -1


    override fun getLayoutResourceId(): Int = R.layout.activity_personnel

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)

            // Настройка заголовка
            supportActionBar?.title = "Персонал"

            // Инициализация ViewModel
            try {
                val database = AppDatabase.getDatabase(this)
                val repository = PersonnelRepository(database.personnelDao())
                val factory = PersonnelViewModelFactory(repository)
                viewModel = ViewModelProvider(this, factory)[PersonnelViewModel::class.java]
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка базы данных: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            // Инициализация views
            initializeViews()
            setupClickListeners()
            setupDoubleTapOnTitle()

            // Обработка параметров из Intent
            handleIntentExtras()

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initializeViews() {
        etLastName = findViewById(R.id.etLastName)
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etPosition = findViewById(R.id.etPosition)
        etCompany = findViewById(R.id.etCompany)
        btnAdd = findViewById(R.id.btnAdd)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupClickListeners() {
        btnAdd.setOnClickListener { addOrUpdatePersonnel() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearFields() }
        findViewById<Button>(R.id.btnViewList).setOnClickListener { openPersonnelList() }
        btnDelete.setOnClickListener { deletePersonnel() }

        // Обработчик кнопки меню
        findViewById<ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupDoubleTapOnTitle() {
        val tvTitle = findViewById<TextView>(R.id.tvPersonnelTitle)
        tvTitle.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                if (System.currentTimeMillis() - lastClickTime < 300) {
                    // Двойное нажатие - открываем меню
                    drawerLayout.openDrawer(GravityCompat.START)
                    lastClickTime = 0
                    return@setOnTouchListener true
                }
                lastClickTime = System.currentTimeMillis()
            }
            false
        }
    }
    
    private fun handleIntentExtras() {
        // Обработка режима редактирования
        isEditMode = intent.getBooleanExtra("edit_mode", false)
        if (isEditMode) {
            editingPersonnelId = intent.getLongExtra("personnel_id", -1)
            val lastName = intent.getStringExtra("last_name") ?: ""
            val firstName = intent.getStringExtra("first_name") ?: ""
            val middleName = intent.getStringExtra("middle_name") ?: ""
            val position = intent.getStringExtra("position") ?: ""
            val company = intent.getStringExtra("company") ?: ""

            etLastName.setText(lastName)
            etFirstName.setText(firstName)
            etMiddleName.setText(middleName)
            etPosition.setText(position)
            etCompany.setText(company)

            // Меняем текст кнопки и показываем кнопку удаления
            btnAdd.text = "Сохранить"
            btnDelete.visibility = View.VISIBLE
            supportActionBar?.title = "Редактирование персонала"
        }

        // Обработка предложенной должности
        val suggestedPosition = intent.getStringExtra("suggested_position")
        if (!suggestedPosition.isNullOrBlank() && !isEditMode) {
            etPosition.setText(suggestedPosition)
        }
    }

    private fun addOrUpdatePersonnel() {
        try {
            val lastName = etLastName.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val middleName = etMiddleName.text.toString().trim()
            val position = etPosition.text.toString().trim()
            val company = etCompany.text.toString().trim()

            if (lastName.isEmpty() || firstName.isEmpty() || position.isEmpty() || company.isEmpty()) {
                Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
                return
            }

            if (isEditMode && editingPersonnelId != -1L) {
                // Обновление существующего сотрудника
                viewModel.updatePersonnel(editingPersonnelId, lastName, firstName, middleName, position, company)
                Toast.makeText(this, "Работник обновлен", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Добавление нового сотрудника
                viewModel.addPersonnel(lastName, firstName, middleName, position, company)
                Toast.makeText(this, "Работник добавлен", Toast.LENGTH_SHORT).show()
                clearFields()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при сохранении: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePersonnel() {
        if (isEditMode && editingPersonnelId != -1L) {
            AlertDialog.Builder(this)
                .setTitle("Удаление работника")
                .setMessage("Вы уверены, что хотите удалить этого работника?")
                .setPositiveButton("Удалить") { _, _ ->
                    try {
                        viewModel.deletePersonnel(editingPersonnelId)
                        Toast.makeText(this, "Работник удален", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Ошибка при удалении: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun clearFields() {
        etLastName.text?.clear()
        etFirstName.text?.clear()
        etMiddleName.text?.clear()
        etPosition.text?.clear()
        etCompany.text?.clear()
    }


    private fun openPersonnelList() {
        val intent = Intent(this, PersonnelListActivity::class.java)
        startActivity(intent)
    }
}
