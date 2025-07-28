package com.example.zmeygorynych

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : BaseActivity() {

    private lateinit var appSettings: AppSettings
    private lateinit var selectedDayColorPreview: View
    private lateinit var currentDayColorPreview: View
    private lateinit var weekendColorPreview: View
    private lateinit var themeRadioGroup: RadioGroup

    override fun getLayoutResourceId(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация настроек
        appSettings = AppSettings.getInstance(this)

        // Настройка заголовка
        supportActionBar?.title = "Настройки"

        // Инициализация views
        initializeViews()

        // Загрузка текущих настроек
        loadCurrentSettings()

        // Настройка обработчиков
        setupListeners()

        // Настройка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        selectedDayColorPreview = findViewById(R.id.selectedDayColorPreview)
        currentDayColorPreview = findViewById(R.id.currentDayColorPreview)
        weekendColorPreview = findViewById(R.id.weekendColorPreview)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
    }

    private fun loadCurrentSettings() {
        // Загружаем цвета
        selectedDayColorPreview.setBackgroundColor(appSettings.selectedDayColor)
        currentDayColorPreview.setBackgroundColor(appSettings.currentDayColor)
        weekendColorPreview.setBackgroundColor(appSettings.weekendColor)

        // Загружаем тему
        when (appSettings.themeMode) {
            AppSettings.ThemeMode.LIGHT -> themeRadioGroup.check(R.id.rbLightTheme)
            AppSettings.ThemeMode.DARK -> themeRadioGroup.check(R.id.rbDarkTheme)
            AppSettings.ThemeMode.SYSTEM -> themeRadioGroup.check(R.id.rbSystemTheme)
        }
    }

    private fun setupListeners() {
        // Обработчик кнопки "Открыть меню"
        val btnOpenMenu = findViewById<Button>(R.id.btnOpenMenu)
        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

                // Обработчики кнопок изменения цветов
        findViewById<Button>(R.id.btnSelectedDayColor).setOnClickListener {
            showColorPicker("Выберите цвет выделенного дня", appSettings.selectedDayColor) { color ->
                appSettings.selectedDayColor = color
                selectedDayColorPreview.setBackgroundColor(color)
                // Обновляем календарь в WorkActivity
                updateCalendarColors()
            }
        }

        findViewById<Button>(R.id.btnCurrentDayColor).setOnClickListener {
            showColorPicker("Выберите цвет текущего дня", appSettings.currentDayColor) { color ->
                appSettings.currentDayColor = color
                currentDayColorPreview.setBackgroundColor(color)
                // Обновляем календарь в WorkActivity
                updateCalendarColors()
            }
        }

        findViewById<Button>(R.id.btnWeekendColor).setOnClickListener {
            showColorPicker("Выберите цвет выходных", appSettings.weekendColor) { color ->
                appSettings.weekendColor = color
                weekendColorPreview.setBackgroundColor(color)
                // Обновляем календарь в WorkActivity
                updateCalendarColors()
            }
        }

        // Обработчик изменения темы
        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                R.id.rbLightTheme -> AppSettings.ThemeMode.LIGHT
                R.id.rbDarkTheme -> AppSettings.ThemeMode.DARK
                R.id.rbSystemTheme -> AppSettings.ThemeMode.SYSTEM
                else -> AppSettings.ThemeMode.SYSTEM
            }
            appSettings.themeMode = themeMode
            applyTheme(themeMode)
        }
    }

    private fun showColorPicker(title: String, initialColor: Int, onColorSelected: (Int) -> Unit) {
        val dialog = ColorPickerDialog.newInstance(title, initialColor, onColorSelected)
        dialog.show(supportFragmentManager, "ColorPicker")
    }

    private fun updateCalendarColors() {
        // В реальном приложении можно использовать EventBus или LiveData
        // для обновления календаря в WorkActivity
        // Пока просто сохраняем настройки, календарь обновится при возврате в WorkActivity
    }

    private fun applyTheme(themeMode: AppSettings.ThemeMode) {
        // Здесь можно добавить логику применения темы
        // Пока просто сохраняем настройку
        // В реальном приложении нужно перезапустить активность или применить тему
    }
}