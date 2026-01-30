package com.example.zmeygorynych

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkDayActivity : BaseActivity() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSave: Button
    private lateinit var spinnerZubov: MaterialAutoCompleteTextView
    private lateinit var spinnerSkd: MaterialAutoCompleteTextView
    private lateinit var spinnerMachinist: MaterialAutoCompleteTextView
    private lateinit var spinnerMachinist2: MaterialAutoCompleteTextView
    private lateinit var spinnerManager: MaterialAutoCompleteTextView
    private lateinit var etPeregon1: TextInputEditText
    private lateinit var etPeregon2: TextInputEditText

    // Поля выбора времени
    private lateinit var etWindowFrom1: TextInputEditText
    private lateinit var etWindowTo1: TextInputEditText
    private lateinit var etWindowFrom2: TextInputEditText
    private lateinit var etWindowTo2: TextInputEditText
    private lateinit var etSkdFrom1: TextInputEditText
    private lateinit var etSkdTo1: TextInputEditText
    private lateinit var etSkdFrom2: TextInputEditText
    private lateinit var etSkdTo2: TextInputEditText
    private lateinit var etZubovFrom1: TextInputEditText
    private lateinit var etZubovTo1: TextInputEditText
    private lateinit var etZubovFrom2: TextInputEditText
    private lateinit var etZubovTo2: TextInputEditText

    // Поля с суммами часов
    private lateinit var tvWindowHoursSum: TextView
    private lateinit var tvSkdHoursSum: TextView
    private lateinit var tvZubovHoursSum: TextView

    private lateinit var personnelRepository: PersonnelRepository
    private lateinit var workTypeRepository: WorkTypeRepository
    private lateinit var workDayRepository: WorkDayRepository

    // Переменные для хранения данных спиннеров
    private var allMachinists: List<Personnel> = emptyList()
    private var allNonManagers: List<Personnel> = emptyList()
    private var currentMachinistQuery: String = ""
    private var currentManagerQuery: String = ""

    // Для отслеживания добавления персонала
    private var lastAddedPositionType: String? = null

    // Activity Result Launcher для обработки возврата из PersonnelActivity
    private val personnelActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Получаем имя добавленного работника
            val addedPersonnelName = result.data?.getStringExtra("added_personnel_name")
            val addedPersonnelPosition = result.data?.getStringExtra("added_personnel_position")

            // После успешного добавления персонала обновляем соответствующие спиннеры
            when (lastAddedPositionType) {
                "машинист" -> {
                    updateMachinistSpinners(addedPersonnelName)
                }
                "руководитель" -> {
                    updateManagerSpinners(addedPersonnelName)
                }
            }
            lastAddedPositionType = null
        }
    }

    // Текущая дата для редактирования
    private var currentDate: Date = Date()

    // Константы для позиций в спиннерах
    companion object {
        private const val EMPTY_POSITION = 0
        private const val ADD_POSITION = 1
        private const val FIRST_PERSONNEL_POSITION = 2
    }

    override fun getLayoutResourceId(): Int = R.layout.activity_work_day

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Настройка заголовка
        supportActionBar?.title = "Рабочий день"

        // Получаем дату из Intent, если передана, иначе используем сегодня
        currentDate = intent.getSerializableExtra("selected_date") as? Date ?: Calendar.getInstance().time

        // Инициализация базы данных
        val database = AppDatabase.getDatabase(this)
        personnelRepository = PersonnelRepository(database.personnelDao())
        workTypeRepository = WorkTypeRepository(database.workTypeDao())
        workDayRepository = WorkDayRepository(database.workDayDao())

        // Инициализация views
        initViews()

        initTimePickers()
        initSpinners()

        // Установка даты
        updateSelectedDate(currentDate)

        // Загрузка существующих данных, если есть
        loadExistingData()

        // Обработчик кнопки сохранения
        btnSave.setOnClickListener {
            saveWorkDay()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Применяем настройки спиннеров
        applySpinnerSettings()

        // Применяем настройки цветов интерфейса
        applyBackgroundColor()
        applyCardBackgroundColor()
    }

    private fun initViews() {
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSave = findViewById(R.id.btnSave)
        spinnerZubov = findViewById(R.id.spinnerZubov)
        spinnerSkd = findViewById(R.id.spinnerSkd)
        spinnerMachinist = findViewById(R.id.spinnerMachinist)
        spinnerMachinist2 = findViewById(R.id.spinnerMachinist2)
        spinnerManager = findViewById(R.id.spinnerManager)
        etPeregon1 = findViewById(R.id.etPeregon1)
        etPeregon2 = findViewById(R.id.etPeregon2)

        // Поля времени
        etWindowFrom1 = findViewById(R.id.etWindowFrom1)
        etWindowTo1 = findViewById(R.id.etWindowTo1)
        etWindowFrom2 = findViewById(R.id.etWindowFrom2)
        etWindowTo2 = findViewById(R.id.etWindowTo2)
        etSkdFrom1 = findViewById(R.id.etSkdFrom1)
        etSkdTo1 = findViewById(R.id.etSkdTo1)
        etSkdFrom2 = findViewById(R.id.etSkdFrom2)
        etSkdTo2 = findViewById(R.id.etSkdTo2)
        etZubovFrom1 = findViewById(R.id.etZubovFrom1)
        etZubovTo1 = findViewById(R.id.etZubovTo1)
        etZubovFrom2 = findViewById(R.id.etZubovFrom2)
        etZubovTo2 = findViewById(R.id.etZubovTo2)

        // Поля сумм часов
        tvWindowHoursSum = findViewById(R.id.tvWindowHoursSum)
        tvSkdHoursSum = findViewById(R.id.tvSkdHoursSum)
        tvZubovHoursSum = findViewById(R.id.tvZubovHoursSum)
    }

    private fun applySpinnerSettings() {
        val appSettings = AppSettings.getInstance(this)

        // Применяем цвет фона к спиннерам
        val spinnerColor = appSettings.spinnerColor
        val cornerRadiusPx = appSettings.spinnerCornerRadius

        // Создаем новый drawable с нужным цветом фона
        val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(spinnerColor)
            cornerRadius = cornerRadiusPx.toFloat()
        }

        // Применяем настройки ко всем TextInputLayout спиннеров (левая часть)
        val textInputLayouts = listOf(
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilZubov),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSkd),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMachinist),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilMachinist2),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilManager),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPeregon1),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPeregon2)
        )

        textInputLayouts.forEach { til ->
            til?.background = backgroundDrawable
        }

        // Применяем ТОТ ЖЕ ЦВЕТ к правой части (поля времени)
        val timeInputLayouts = listOf(
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilWindowFrom1),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilWindowTo1),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilWindowFrom2),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilWindowTo2),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSkdFrom1),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSkdTo1),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSkdFrom2),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSkdTo2),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilZubovFrom1),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilZubovTo1),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilZubovFrom2),
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilZubovTo2)
        )

        timeInputLayouts.forEach { til ->
            til?.background = backgroundDrawable
        }
    }

    private fun loadExistingData() {
        lifecycleScope.launch {
            try {
                val existingWorkDay = workDayRepository.getWorkDayByDate(currentDate.time)
                existingWorkDay?.let { workDay ->
                    // Заполняем спиннеры
                    setSpinnerSelection(spinnerZubov, workDay.zubovWorkType)
                    setSpinnerSelection(spinnerSkd, workDay.skdWorkType)
                    setSpinnerSelection(spinnerMachinist, workDay.machinist1)
                    setSpinnerSelection(spinnerMachinist2, workDay.machinist2)
                    setSpinnerSelection(spinnerManager, workDay.manager)

                    // Заполняем время
                    etWindowFrom1.setText(workDay.windowFrom1.orEmpty())
                    etWindowTo1.setText(workDay.windowTo1.orEmpty())
                    etWindowFrom2.setText(workDay.windowFrom2.orEmpty())
                    etWindowTo2.setText(workDay.windowTo2.orEmpty())
                    etSkdFrom1.setText(workDay.skdFrom1.orEmpty())
                    etSkdTo1.setText(workDay.skdTo1.orEmpty())
                    etSkdFrom2.setText(workDay.skdFrom2.orEmpty())
                    etSkdTo2.setText(workDay.skdTo2.orEmpty())
                    etZubovFrom1.setText(workDay.zubovFrom1.orEmpty())
                    etZubovTo1.setText(workDay.zubovTo1.orEmpty())
                    etZubovFrom2.setText(workDay.zubovFrom2.orEmpty())
                    etZubovTo2.setText(workDay.zubovTo2.orEmpty())

                    // Заполняем суммы часов
                    tvWindowHoursSum.text = workDay.getWindowHoursTotal().takeIf { it > 0 }?.toString() ?: ""
                    tvSkdHoursSum.text = workDay.getSkdHoursTotal().takeIf { it > 0 }?.toString() ?: ""
                    tvZubovHoursSum.text = workDay.getZubovHoursTotal().takeIf { it > 0 }?.toString() ?: ""

                    // Пересчитываем часы (на всякий случай)
                    recalculateAllHours()
                }
            } catch (e: Exception) {
                // В случае ошибки ничего не делаем
            }
        }
    }

    private fun setSpinnerSelection(spinner: MaterialAutoCompleteTextView, value: String?) {
        if (!value.isNullOrBlank()) {
            spinner.setText(value, false)
        }
    }

    private fun saveWorkDay() {
        lifecycleScope.launch {
            try {
                // Нормализуем дату к началу дня
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val normalizedDate = calendar.time

                val workDay = WorkDay(
                    date = normalizedDate.time,
                    zubovWorkType = getSpinnerValue(spinnerZubov),
                    skdWorkType = getSpinnerValue(spinnerSkd),
                    machinist1 = getSpinnerValue(spinnerMachinist),
                    machinist2 = getSpinnerValue(spinnerMachinist2),
                    manager = getSpinnerValue(spinnerManager),
                    peregon1 = etPeregon1.text?.toString().takeIf { it?.isNotBlank() == true },
                    peregon2 = etPeregon2.text?.toString().takeIf { it?.isNotBlank() == true },
                    windowFrom1 = etWindowFrom1.text?.toString().takeIf { it?.isNotBlank() == true },
                    windowTo1 = etWindowTo1.text?.toString().takeIf { it?.isNotBlank() == true },
                    windowFrom2 = etWindowFrom2.text?.toString().takeIf { it?.isNotBlank() == true },
                    windowTo2 = etWindowTo2.text?.toString().takeIf { it?.isNotBlank() == true },
                    skdFrom1 = etSkdFrom1.text?.toString().takeIf { it?.isNotBlank() == true },
                    skdTo1 = etSkdTo1.text?.toString().takeIf { it?.isNotBlank() == true },
                    skdFrom2 = etSkdFrom2.text?.toString().takeIf { it?.isNotBlank() == true },
                    skdTo2 = etSkdTo2.text?.toString().takeIf { it?.isNotBlank() == true },
                    zubovFrom1 = etZubovFrom1.text?.toString().takeIf { it?.isNotBlank() == true },
                    zubovTo1 = etZubovTo1.text?.toString().takeIf { it?.isNotBlank() == true },
                    zubovFrom2 = etZubovFrom2.text?.toString().takeIf { it?.isNotBlank() == true },
                    zubovTo2 = etZubovTo2.text?.toString().takeIf { it?.isNotBlank() == true },
                    // Сохраняем суммы
                    windowHoursSum = tvWindowHoursSum.text?.toString()?.toIntOrNull(),
                    skdHoursSum = tvSkdHoursSum.text?.toString()?.toIntOrNull(),
                    zubovHoursSum = tvZubovHoursSum.text?.toString()?.toIntOrNull()
                )

                workDayRepository.saveWorkDay(workDay)
                android.util.Log.d("WorkDayActivity", "Work day saved: $workDay")
                Toast.makeText(this@WorkDayActivity, "Данные сохранены", Toast.LENGTH_SHORT).show()
                finish() // Закрываем активити после сохранения
            } catch (e: Exception) {
                Toast.makeText(this@WorkDayActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getSpinnerValue(spinner: MaterialAutoCompleteTextView): String? {
        val text = spinner.text?.toString()
        return if (text.isNullOrBlank() || text == "—") null else text
    }

    private fun initTimePickers() {
        val timeFields = listOf(
            etWindowFrom1, etWindowTo1, etWindowFrom2, etWindowTo2,
            etSkdFrom1, etSkdTo1, etSkdFrom2, etSkdTo2,
            etZubovFrom1, etZubovTo1, etZubovFrom2, etZubovTo2
        )

        timeFields.forEach { editText ->
            editText.setOnClickListener {
                showTimePicker(editText)
            }
        }
    }

    private fun showTimePicker(target: TextInputEditText) {
        // Стартуем с текущего значения, если оно задано, иначе с 00:00
        val current = target.text?.toString().orEmpty()
        val (hour, minute) = current.split(":").let { parts ->
            val h = parts.getOrNull(0)?.toIntOrNull()
            val m = parts.getOrNull(1)?.toIntOrNull()
            if (h != null && m != null) h to m else 0 to 0
        }

        val listener = TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            val timeText = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            target.setText(timeText)
            recalculateAllHours()
        }

        val dialog = TimePickerDialog(this, listener, hour, minute, true)
        dialog.show()
    }

    private fun recalculateAllHours() {
        // Считаем суммы для каждой группы
        calculateSumForGroup(
            listOf(
                Pair(etWindowFrom1, etWindowTo1),
                Pair(etWindowFrom2, etWindowTo2)
            ),
            tvWindowHoursSum
        )

        calculateSumForGroup(
            listOf(
                Pair(etSkdFrom1, etSkdTo1),
                Pair(etSkdFrom2, etSkdTo2)
            ),
            tvSkdHoursSum
        )

        calculateSumForGroup(
            listOf(
                Pair(etZubovFrom1, etZubovTo1),
                Pair(etZubovFrom2, etZubovTo2)
            ),
            tvZubovHoursSum
        )
    }

    private fun calculateSumForGroup(pairs: List<Pair<TextInputEditText, TextInputEditText>>, sumView: TextView) {
        var totalHours = 0

        pairs.forEach { (fromView, toView) ->
            val fromText = fromView.text?.toString() ?: ""
            val toText = toView.text?.toString() ?: ""

            if (fromText.isNotBlank() && toText.isNotBlank()) {
                totalHours += calculateHoursForPair(fromText, toText)
            }
        }

        if (totalHours > 0) {
            sumView.text = totalHours.toString()
        } else {
            sumView.text = ""
        }
    }

    private fun calculateHoursForPair(fromText: String, toText: String): Int {
        fun parseTime(value: String): Int? {
            val parts = value.split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            return h * 60 + m
        }

        val fromMinutes = parseTime(fromText)
        val toMinutes = parseTime(toText)

        if (fromMinutes == null || toMinutes == null) return 0

        val diffMinutes = toMinutes - fromMinutes
        if (diffMinutes <= 0) return 0

        return (diffMinutes + 59) / 60 // округление вверх
    }

    private fun initSpinners() {
        // Инициализация спиннеров с данными из базы
        lifecycleScope.launch {
            try {
                // Получаем все данные из базы данных
                allMachinists = personnelRepository.getMachinists()
                allNonManagers = personnelRepository.getNonManagers()

                // Загружаем существующие данные для выбранной даты
                loadExistingWorkDayData()

                // Инициализируем спиннеры с полными данными
                updateMachinistSpinners()
                updateManagerSpinners()

                // Инициализируем спиннеры видов работ
                updateWorkTypeSpinners("зубов", spinnerZubov)
                updateWorkTypeSpinners("скд", spinnerSkd)

            } catch (e: Exception) {
                // В случае ошибки используем временные данные
                initSpinnersWithFallback()
            }
        }
    }

    private suspend fun loadExistingWorkDayData() {
        try {
            // Нормализуем дату к началу дня
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val normalizedDate = calendar.time

            // Загружаем существующий рабочий день для выбранной даты
            val existingWorkDay = workDayRepository.getWorkDayByDate(normalizedDate.time)

            android.util.Log.d("WorkDayActivity", "Loading existing work day for ${normalizedDate.time}: $existingWorkDay")

            if (existingWorkDay != null) {
                // Заполняем спиннеры данными из сохраненного рабочего дня
                setSpinnerSelection(spinnerZubov, existingWorkDay.zubovWorkType)
                setSpinnerSelection(spinnerSkd, existingWorkDay.skdWorkType)
                setSpinnerSelection(spinnerMachinist, existingWorkDay.machinist1)
                setSpinnerSelection(spinnerMachinist2, existingWorkDay.machinist2)
                setSpinnerSelection(spinnerManager, existingWorkDay.manager)

                // Заполняем текстовые поля перегонов
                etPeregon1.setText(existingWorkDay.peregon1 ?: "")
                etPeregon2.setText(existingWorkDay.peregon2 ?: "")

                // Заполняем поля времени
                etWindowFrom1.setText(existingWorkDay.windowFrom1.orEmpty())
                etWindowTo1.setText(existingWorkDay.windowTo1.orEmpty())
                etWindowFrom2.setText(existingWorkDay.windowFrom2.orEmpty())
                etWindowTo2.setText(existingWorkDay.windowTo2.orEmpty())

                etSkdFrom1.setText(existingWorkDay.skdFrom1.orEmpty())
                etSkdTo1.setText(existingWorkDay.skdTo1.orEmpty())
                etSkdFrom2.setText(existingWorkDay.skdFrom2.orEmpty())
                etSkdTo2.setText(existingWorkDay.skdTo2.orEmpty())

                etZubovFrom1.setText(existingWorkDay.zubovFrom1.orEmpty())
                etZubovTo1.setText(existingWorkDay.zubovTo1.orEmpty())
                etZubovFrom2.setText(existingWorkDay.zubovFrom2.orEmpty())
                etZubovTo2.setText(existingWorkDay.zubovTo2.orEmpty())

                // Заполняем суммы часов
                tvWindowHoursSum.text = existingWorkDay.getWindowHoursTotal().takeIf { it > 0 }?.toString() ?: ""
                tvSkdHoursSum.text = existingWorkDay.getSkdHoursTotal().takeIf { it > 0 }?.toString() ?: ""
                tvZubovHoursSum.text = existingWorkDay.getZubovHoursTotal().takeIf { it > 0 }?.toString() ?: ""

                // Применяем настройки после загрузки данных
                applySpinnerSettings()
                applyBackgroundColor()
                applyCardBackgroundColor()

                // Пересчитываем часы (на всякий случай)
                recalculateAllHours()
            }
        } catch (e: Exception) {
            // В случае ошибки загрузки данных просто продолжаем без них
            // Данные будут пустыми, что нормально для нового дня
        }
    }

    private fun <T> MaterialAutoCompleteTextView.bind(items: List<T>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, items)
        this.setAdapter(adapter)
        this.setDropDownBackgroundResource(R.drawable.spinner_dropdown_background)

        // Добавляем обработчик клика для открытия dropdown
        this.setOnClickListener {
            if (!this.isPopupShowing) {
                this.showDropDown()
            }
        }

        // Также добавляем обработчик для TextInputLayout (родительского контейнера)
        (this.parent as? com.google.android.material.textfield.TextInputLayout)?.setOnClickListener {
            if (!this.isPopupShowing) {
                this.showDropDown()
            }
        }
    }

    private fun updateMachinistSpinners(selectItem: String? = null) {
        val machinistItems = mutableListOf<String>()
        machinistItems.add("") // Пустая строка
        machinistItems.add("Добавить...") // Опция добавления

        val filteredMachinists = if (currentMachinistQuery.isBlank()) {
            allMachinists
        } else {
            allMachinists.filter {
                it.fullName.contains(currentMachinistQuery, ignoreCase = true)
            }
        }

        machinistItems.addAll(filteredMachinists.map { it.fullName })

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, machinistItems)

        // Сохраняем текущий текст
        val currentText1 = spinnerMachinist.text?.toString() ?: ""
        val currentText2 = spinnerMachinist2.text?.toString() ?: ""

        spinnerMachinist.setAdapter(adapter)
        spinnerMachinist.setDropDownBackgroundResource(R.drawable.spinner_dropdown_background)
        spinnerMachinist.setOnClickListener {
            if (!spinnerMachinist.isPopupShowing) {
                spinnerMachinist.showDropDown()
            }
        }
        (spinnerMachinist.parent as? com.google.android.material.textfield.TextInputLayout)?.setOnClickListener {
            if (!spinnerMachinist.isPopupShowing) {
                spinnerMachinist.showDropDown()
            }
        }

        spinnerMachinist2.setAdapter(adapter)
        spinnerMachinist2.setDropDownBackgroundResource(R.drawable.spinner_dropdown_background)
        spinnerMachinist2.setOnClickListener {
            if (!spinnerMachinist2.isPopupShowing) {
                spinnerMachinist2.showDropDown()
            }
        }
        (spinnerMachinist2.parent as? com.google.android.material.textfield.TextInputLayout)?.setOnClickListener {
            if (!spinnerMachinist2.isPopupShowing) {
                spinnerMachinist2.showDropDown()
            }
        }

        // Выбираем добавленный элемент или восстанавливаем предыдущий текст
        if (selectItem != null && machinistItems.contains(selectItem)) {
            // Если передан элемент для выбора, выбираем его в первом доступном спиннере
            if (spinnerMachinist.text.isNullOrBlank()) {
                spinnerMachinist.setText(selectItem, false)
            } else if (spinnerMachinist2.text.isNullOrBlank()) {
                spinnerMachinist2.setText(selectItem, false)
            } else {
                // Если оба спиннера заняты, выбираем в первом
                spinnerMachinist.setText(selectItem, false)
            }
        } else {
            // Восстанавливаем предыдущий текст
            if (machinistItems.contains(currentText1)) {
                spinnerMachinist.setText(currentText1, false)
            }
            if (machinistItems.contains(currentText2)) {
                spinnerMachinist2.setText(currentText2, false)
            }
        }

        // Добавляем обработчики событий
        setupMachinistSpinnerListeners()
    }

    private fun updateManagerSpinners(selectItem: String? = null) {
        val managerItems = mutableListOf<String>()
        managerItems.add("") // Пустая строка
        managerItems.add("Добавить...") // Опция добавления

        // allNonManagers содержит работников, которые НЕ являются машинистами (включая руководителей)
        val filteredManagers = if (currentManagerQuery.isBlank()) {
            allNonManagers
        } else {
            allNonManagers.filter {
                it.fullName.contains(currentManagerQuery, ignoreCase = true)
            }
        }

        managerItems.addAll(filteredManagers.map { it.fullName })

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, managerItems)

        // Сохраняем текущий текст
        val currentText = spinnerManager.text?.toString() ?: ""

        spinnerManager.setAdapter(adapter)
        spinnerManager.setDropDownBackgroundResource(R.drawable.spinner_dropdown_background)
        spinnerManager.setOnClickListener {
            if (!spinnerManager.isPopupShowing) {
                spinnerManager.showDropDown()
            }
        }
        (spinnerManager.parent as? com.google.android.material.textfield.TextInputLayout)?.setOnClickListener {
            if (!spinnerManager.isPopupShowing) {
                spinnerManager.showDropDown()
            }
        }

        // Выбираем добавленный элемент или восстанавливаем предыдущий текст
        if (selectItem != null && managerItems.contains(selectItem)) {
            // Если передан элемент для выбора, выбираем его
            spinnerManager.setText(selectItem, false)
        } else if (managerItems.contains(currentText)) {
            // Восстанавливаем предыдущий текст
            spinnerManager.setText(currentText, false)
        }

        // Добавляем обработчик событий
        setupManagerSpinnerListeners()
    }

    // Методы для поиска (можно вызывать извне)
    fun searchMachinists(query: String) {
        currentMachinistQuery = query
        updateMachinistSpinners()
    }

    fun searchManagers(query: String) {
        currentManagerQuery = query
        updateManagerSpinners()
    }

    // Пример использования поиска (можно вызывать из других частей приложения)
    fun performSearch(query: String) {
        searchMachinists(query)
        searchManagers(query)
    }

    // Сброс поиска - показать всех работников
    fun resetSearch() {
        currentMachinistQuery = ""
        currentManagerQuery = ""
        updateMachinistSpinners()
        updateManagerSpinners()
    }

    private fun setupMachinistSpinnerListeners() {
        // Обработчик для первого спиннера машинистов
        spinnerMachinist.setOnItemClickListener { parent, view, position, id ->
            if (position == ADD_POSITION) {
                openPersonnelActivity("машинист")
                // Очищаем текст
                spinnerMachinist.setText("", false)
            }
        }

        // Обработчик для второго спиннера машинистов
        spinnerMachinist2.setOnItemClickListener { parent, view, position, id ->
            if (position == ADD_POSITION) {
                openPersonnelActivity("машинист")
                // Очищаем текст
                spinnerMachinist2.setText("", false)
            }
        }
    }

    private fun setupManagerSpinnerListeners() {
        // Обработчик для спиннера руководителей
        spinnerManager.setOnItemClickListener { parent, view, position, id ->
            if (position == ADD_POSITION) {
                openPersonnelActivity("руководитель")
                // Очищаем текст
                spinnerManager.setText("", false)
            }
        }
    }

    private fun openPersonnelActivity(position: String) {
        lastAddedPositionType = position
        val intent = Intent(this, PersonnelActivity::class.java).apply {
            putExtra("suggested_position", position)
        }
        personnelActivityResult.launch(intent)
    }

    private fun initSpinnersWithFallback() {
        // Временные списки значений в случае ошибки
        val zubovItems = listOf("", "Прогрев рельсов плети", "Обслуживание и ремонт", "Заправка дизтопливом", "Добавить...")
        val skdItems = listOf("", "Подача воздуха", "Обслуживание и ремонт", "Заправка дизтопливом", "Добавить...")
        val machinistItems = listOf("", "Добавить...", "Машинист 1", "Машинист 2")
        val managerItems = listOf("", "Добавить...", "Руководитель 1", "Руководитель 2")
        spinnerZubov.bind(zubovItems)
        spinnerSkd.bind(skdItems)
        spinnerMachinist.bind(machinistItems)
        spinnerMachinist2.bind(machinistItems)
        spinnerManager.bind(managerItems)

        setupSpinnerListenersForFallback(spinnerZubov, "зубов")
        setupSpinnerListenersForFallback(spinnerSkd, "скд")
        setupMachinistSpinnerListeners()
        setupManagerSpinnerListeners()
    }

    private fun setupSpinnerListenersForFallback(spinner: MaterialAutoCompleteTextView, type: String) {
        spinner.setOnItemClickListener { parent, view, position, id ->
            if (position == (spinner.adapter?.count ?: 0) - 1) { // Если выбрана последняя опция ("Добавить...")
                showAddWorkTypeDialog(type)
                spinner.setText("", false) // Очищаем текст
            }
        }
    }

    private fun updateSelectedDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val monthName = SimpleDateFormat("MMMM", Locale("ru")).format(date)
        tvSelectedDate.text = "$day $monthName $year года"
    }

    private fun updateWorkTypeSpinners(type: String, spinner: MaterialAutoCompleteTextView, selectItem: String? = null) {
        lifecycleScope.launch {
            workTypeRepository.getWorkTypesByType(type).collect { workTypes ->
                val items = mutableListOf("") // Пустая строка
                items.addAll(workTypes.map { it.name })
                items.add("Добавить...") // Опция добавления

                val adapter = ArrayAdapter(this@WorkDayActivity, android.R.layout.simple_dropdown_item_1line, items)
                spinner.setAdapter(adapter)
                spinner.setDropDownBackgroundResource(R.drawable.spinner_dropdown_background)
                spinner.setOnClickListener {
                    if (!spinner.isPopupShowing) {
                        spinner.showDropDown()
                    }
                }
                (spinner.parent as? com.google.android.material.textfield.TextInputLayout)?.setOnClickListener {
                    if (!spinner.isPopupShowing) {
                        spinner.showDropDown()
                    }
                }

                // Сохраняем текущий текст
                val currentText = spinner.text?.toString() ?: ""

                // Восстанавливаем текст, если он есть в списке
                if (selectItem != null && items.contains(selectItem)) {
                    // Если передан элемент для выбора, выбираем его
                    spinner.setText(selectItem, false)
                } else if (items.contains(currentText)) {
                    // Иначе восстанавливаем предыдущий текст
                    spinner.setText(currentText, false)
                }

                spinner.setOnItemClickListener { parent, view, position, id ->
                    if (position == items.size - 1) { // Если выбрана последняя опция ("Добавить...")
                        showAddWorkTypeDialog(type)
                        spinner.setText("", false) // Очищаем текст
                    }
                }
            }
        }
    }

    private fun showAddWorkTypeDialog(type: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Добавить вид работы")

        val input = android.widget.EditText(this)
        input.hint = "Введите название вида работы"
        builder.setView(input)

        builder.setPositiveButton("Добавить") { dialog, _ ->
            val workTypeName = input.text.toString().trim()
            if (workTypeName.isNotEmpty()) {
                lifecycleScope.launch {
                    workTypeRepository.insert(WorkType(name = workTypeName, type = type))
                    // После добавления обновим спиннеры и выберем новый элемент
                    updateWorkTypeSpinners(type, if (type == "зубов") spinnerZubov else spinnerSkd, workTypeName)
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun applyBackgroundColor() {
        val appSettings = AppSettings.getInstance(this)
        // Изменяем цвет фона текущей активности
        val rootView = findViewById<View>(R.id.main)
        rootView.setBackgroundColor(appSettings.backgroundColor)
    }

    private fun applyCardBackgroundColor() {
        val appSettings = AppSettings.getInstance(this)
        // Изменяем цвет фона основной карточки
        val cardView = findViewById<androidx.cardview.widget.CardView>(R.id.cardView)
        cardView.setCardBackgroundColor(appSettings.cardBackgroundColor)
    }
}