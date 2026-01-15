package com.example.zmeygorynych

import android.content.Intent
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WorkDayActivity : BaseActivity() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSave: Button
    private lateinit var spinnerZubov: Spinner
    private lateinit var spinnerSkd: Spinner
    private lateinit var spinnerMachinist: Spinner
    private lateinit var spinnerMachinist2: Spinner
    private lateinit var spinnerManager: Spinner
    private lateinit var spinnerPeregon1: Spinner
    private lateinit var spinnerPeregon2: Spinner
    // Поля выбора времени
    private lateinit var tvWindowFrom1: TextView
    private lateinit var tvWindowTo1: TextView
    private lateinit var tvWindowFrom2: TextView
    private lateinit var tvWindowTo2: TextView
    private lateinit var tvSkdFrom1: TextView
    private lateinit var tvSkdTo1: TextView
    private lateinit var tvSkdFrom2: TextView
    private lateinit var tvSkdTo2: TextView
    private lateinit var tvZubovFrom1: TextView
    private lateinit var tvZubovTo1: TextView
    private lateinit var tvZubovFrom2: TextView
    private lateinit var tvZubovTo2: TextView
    // Поля с количеством часов
    private lateinit var tvWindowHours1: TextView
    private lateinit var tvWindowHours2: TextView
    private lateinit var tvSkdHours1: TextView
    private lateinit var tvSkdHours2: TextView
    private lateinit var tvZubovHours1: TextView
    private lateinit var tvZubovHours2: TextView

    private lateinit var personnelRepository: PersonnelRepository
    private lateinit var workTypeRepository: WorkTypeRepository
    private lateinit var workDayRepository: WorkDayRepository

    // Переменные для хранения данных спиннеров
    private var allMachinists: List<Personnel> = emptyList()
    private var allNonManagers: List<Personnel> = emptyList()
    private var currentMachinistQuery: String = ""
    private var currentManagerQuery: String = ""

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
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSave = findViewById(R.id.btnSave)
        spinnerZubov = findViewById(R.id.spinnerZubov)
        spinnerSkd = findViewById(R.id.spinnerSkd)
        spinnerMachinist = findViewById(R.id.spinnerMachinist)
        spinnerMachinist2 = findViewById(R.id.spinnerMachinist2)
        spinnerManager = findViewById(R.id.spinnerManager)
        spinnerPeregon1 = findViewById(R.id.spinnerPeregon1)
        spinnerPeregon2 = findViewById(R.id.spinnerPeregon2)
        // Поля времени
        tvWindowFrom1 = findViewById(R.id.tvWindowFrom1)
        tvWindowTo1 = findViewById(R.id.tvWindowTo1)
        tvWindowFrom2 = findViewById(R.id.tvWindowFrom2)
        tvWindowTo2 = findViewById(R.id.tvWindowTo2)
        tvSkdFrom1 = findViewById(R.id.tvSkdFrom1)
        tvSkdTo1 = findViewById(R.id.tvSkdTo1)
        tvSkdFrom2 = findViewById(R.id.tvSkdFrom2)
        tvSkdTo2 = findViewById(R.id.tvSkdTo2)
        tvZubovFrom1 = findViewById(R.id.tvZubovFrom1)
        tvZubovTo1 = findViewById(R.id.tvZubovTo1)
        tvZubovFrom2 = findViewById(R.id.tvZubovFrom2)
        tvZubovTo2 = findViewById(R.id.tvZubovTo2)
        // Поля часов
        tvWindowHours1 = findViewById(R.id.tvWindowHours1)
        tvWindowHours2 = findViewById(R.id.tvWindowHours2)
        tvSkdHours1 = findViewById(R.id.tvSkdHours1)
        tvSkdHours2 = findViewById(R.id.tvSkdHours2)
        tvZubovHours1 = findViewById(R.id.tvZubovHours1)
        tvZubovHours2 = findViewById(R.id.tvZubovHours2)

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
                    setSpinnerSelection(spinnerPeregon1, workDay.peregon1)
                    setSpinnerSelection(spinnerPeregon2, workDay.peregon2)

                    // Заполняем время
                    tvWindowFrom1.text = workDay.windowFrom1 ?: "--:--"
                    tvWindowTo1.text = workDay.windowTo1 ?: "--:--"
                    tvWindowFrom2.text = workDay.windowFrom2 ?: "--:--"
                    tvWindowTo2.text = workDay.windowTo2 ?: "--:--"
                    tvSkdFrom1.text = workDay.skdFrom1 ?: "--:--"
                    tvSkdTo1.text = workDay.skdTo1 ?: "--:--"
                    tvSkdFrom2.text = workDay.skdFrom2 ?: "--:--"
                    tvSkdTo2.text = workDay.skdTo2 ?: "--:--"
                    tvZubovFrom1.text = workDay.zubovFrom1 ?: "--:--"
                    tvZubovTo1.text = workDay.zubovTo1 ?: "--:--"
                    tvZubovFrom2.text = workDay.zubovFrom2 ?: "--:--"
                    tvZubovTo2.text = workDay.zubovTo2 ?: "--:--"

                    // Пересчитываем часы
                    recalculateAllHours()
                }
            } catch (e: Exception) {
                // В случае ошибки ничего не делаем
            }
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        if (value.isNullOrBlank()) return

        for (i in 0 until spinner.adapter.count) {
            if (spinner.adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun saveWorkDay() {
        lifecycleScope.launch {
            try {
                val workDay = WorkDay(
                    date = currentDate.time,
                    zubovWorkType = getSpinnerValue(spinnerZubov),
                    skdWorkType = getSpinnerValue(spinnerSkd),
                    machinist1 = getSpinnerValue(spinnerMachinist),
                    machinist2 = getSpinnerValue(spinnerMachinist2),
                    manager = getSpinnerValue(spinnerManager),
                    peregon1 = getSpinnerValue(spinnerPeregon1),
                    peregon2 = getSpinnerValue(spinnerPeregon2),
                    windowFrom1 = tvWindowFrom1.text?.toString().takeIf { it != "--:--" },
                    windowTo1 = tvWindowTo1.text?.toString().takeIf { it != "--:--" },
                    windowFrom2 = tvWindowFrom2.text?.toString().takeIf { it != "--:--" },
                    windowTo2 = tvWindowTo2.text?.toString().takeIf { it != "--:--" },
                    skdFrom1 = tvSkdFrom1.text?.toString().takeIf { it != "--:--" },
                    skdTo1 = tvSkdTo1.text?.toString().takeIf { it != "--:--" },
                    skdFrom2 = tvSkdFrom2.text?.toString().takeIf { it != "--:--" },
                    skdTo2 = tvSkdTo2.text?.toString().takeIf { it != "--:--" },
                    zubovFrom1 = tvZubovFrom1.text?.toString().takeIf { it != "--:--" },
                    zubovTo1 = tvZubovTo1.text?.toString().takeIf { it != "--:--" },
                    zubovFrom2 = tvZubovFrom2.text?.toString().takeIf { it != "--:--" },
                    zubovTo2 = tvZubovTo2.text?.toString().takeIf { it != "--:--" },
                    windowHours1 = tvWindowHours1.text?.toString()?.toIntOrNull(),
                    windowHours2 = tvWindowHours2.text?.toString()?.toIntOrNull(),
                    skdHours1 = tvSkdHours1.text?.toString()?.toIntOrNull(),
                    skdHours2 = tvSkdHours2.text?.toString()?.toIntOrNull(),
                    zubovHours1 = tvZubovHours1.text?.toString()?.toIntOrNull(),
                    zubovHours2 = tvZubovHours2.text?.toString()?.toIntOrNull()
                )

                workDayRepository.saveWorkDay(workDay)
                Toast.makeText(this@WorkDayActivity, "Данные сохранены", Toast.LENGTH_SHORT).show()
                finish() // Закрываем активити после сохранения
            } catch (e: Exception) {
                Toast.makeText(this@WorkDayActivity, "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getSpinnerValue(spinner: Spinner): String? {
        val selectedItem = spinner.selectedItem?.toString()
        return if (selectedItem.isNullOrBlank() || selectedItem == "—") null else selectedItem
    }

    private fun initTimePickers() {
        val timeFields = listOf(
            tvWindowFrom1, tvWindowTo1, tvWindowFrom2, tvWindowTo2,
            tvSkdFrom1, tvSkdTo1, tvSkdFrom2, tvSkdTo2,
            tvZubovFrom1, tvZubovTo1, tvZubovFrom2, tvZubovTo2
        )

        timeFields.forEach { textView ->
            textView.setOnClickListener {
                showTimePicker(textView)
            }
        }
    }

    private fun showTimePicker(target: TextView) {
        // Стартуем всегда с 00:00
        val hour = 0
        val minute = 0

        val listener = TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            val timeText = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            target.text = timeText
            recalculateAllHours()
        }

        val dialog = TimePickerDialog(this, listener, hour, minute, true)
        dialog.show()
    }

    private fun recalculateAllHours() {
        calculateHoursForPair(tvWindowFrom1, tvWindowTo1, tvWindowHours1)
        calculateHoursForPair(tvWindowFrom2, tvWindowTo2, tvWindowHours2)
        calculateHoursForPair(tvSkdFrom1, tvSkdTo1, tvSkdHours1)
        calculateHoursForPair(tvSkdFrom2, tvSkdTo2, tvSkdHours2)
        calculateHoursForPair(tvZubovFrom1, tvZubovTo1, tvZubovHours1)
        calculateHoursForPair(tvZubovFrom2, tvZubovTo2, tvZubovHours2)
    }

    private fun calculateHoursForPair(fromView: TextView, toView: TextView, hoursView: TextView) {
        val fromText = fromView.text?.toString() ?: ""
        val toText = toView.text?.toString() ?: ""

        if (fromText == "--:--" || toText == "--:--" || fromText.isBlank() || toText.isBlank()) {
            hoursView.text = ""
            return
        }

        fun parseTime(value: String): Int? {
            val parts = value.split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            return h * 60 + m
        }

        val fromMinutes = parseTime(fromText)
        val toMinutes = parseTime(toText)

        if (fromMinutes == null || toMinutes == null) {
            hoursView.text = ""
            return
        }

        val diffMinutes = toMinutes - fromMinutes
        if (diffMinutes <= 0) {
            hoursView.text = ""
            return
        }

        val fullHoursRoundedUp = (diffMinutes + 59) / 60 // округление вверх
        hoursView.text = fullHoursRoundedUp.toString()
    }

    private fun initSpinners() {
        // Инициализация спиннеров с данными из базы
        lifecycleScope.launch {
            try {
                // Получаем все данные из базы данных
                allMachinists = personnelRepository.getMachinists()
                allNonManagers = personnelRepository.getNonManagers()

                // Инициализируем спиннеры с полными данными
                updateMachinistSpinners()
                updateManagerSpinners()

                // Остальные спиннеры с данными
                val peregonItems = listOf("—", "Перегон А-Б", "Перегон В-Г", "Перегон Д-Е")

                // Привязываем данные к спиннерам
                spinnerPeregon1.bind(peregonItems)
                spinnerPeregon2.bind(peregonItems)

                // Инициализируем спиннеры видов работ
                updateWorkTypeSpinners("зубов", spinnerZubov)
                updateWorkTypeSpinners("скд", spinnerSkd)

            } catch (e: Exception) {
                // В случае ошибки используем временные данные
                initSpinnersWithFallback()
            }
        }
    }

    private fun <T> Spinner.bind(items: List<T>) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.adapter = adapter
    }

    private fun updateMachinistSpinners() {
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

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, machinistItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Сохраняем текущие позиции
        val currentPosition1 = spinnerMachinist.selectedItemPosition
        val currentPosition2 = spinnerMachinist2.selectedItemPosition

        spinnerMachinist.adapter = adapter
        spinnerMachinist2.adapter = adapter

        // Восстанавливаем позиции если они валидны
        if (currentPosition1 < machinistItems.size) {
            spinnerMachinist.setSelection(currentPosition1)
        }
        if (currentPosition2 < machinistItems.size) {
            spinnerMachinist2.setSelection(currentPosition2)
        }

        // Добавляем обработчики событий
        setupMachinistSpinnerListeners()
    }

    private fun updateManagerSpinners() {
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

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, managerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Сохраняем текущую позицию
        val currentPosition = spinnerManager.selectedItemPosition

        spinnerManager.adapter = adapter

        // Восстанавливаем позицию если она валидна
        if (currentPosition < managerItems.size) {
            spinnerManager.setSelection(currentPosition)
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
        spinnerMachinist.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == ADD_POSITION) {
                    openPersonnelActivity("машинист")
                    // Возвращаем к пустой позиции
                    spinnerMachinist.setSelection(EMPTY_POSITION)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Обработчик для второго спиннера машинистов
        spinnerMachinist2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == ADD_POSITION) {
                    openPersonnelActivity("машинист")
                    // Возвращаем к пустой позиции
                    spinnerMachinist2.setSelection(EMPTY_POSITION)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupManagerSpinnerListeners() {
        // Обработчик для спиннера руководителей
        spinnerManager.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == ADD_POSITION) {
                    openPersonnelActivity("руководитель")
                    // Возвращаем к пустой позиции
                    spinnerManager.setSelection(EMPTY_POSITION)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun openPersonnelActivity(position: String) {
        val intent = Intent(this, PersonnelActivity::class.java).apply {
            putExtra("suggested_position", position)
        }
        startActivity(intent)
    }

    private fun initSpinnersWithFallback() {
        // Временные списки значений в случае ошибки
        val zubovItems = listOf("", "Прогрев рельсов плети", "Обслуживание и ремонт", "Заправка дизтопливом", "Добавить...")
        val skdItems = listOf("", "Подача воздуха", "Обслуживание и ремонт", "Заправка дизтопливом", "Добавить...")
        val machinistItems = listOf("", "Добавить...", "Машинист 1", "Машинист 2")
        val managerItems = listOf("", "Добавить...", "Руководитель 1", "Руководитель 2")
        val peregonItems = listOf("—", "Перегон А-Б", "Перегон В-Г", "Перегон Д-Е")

        spinnerZubov.bind(zubovItems)
        spinnerSkd.bind(skdItems)
        spinnerMachinist.bind(machinistItems)
        spinnerMachinist2.bind(machinistItems)
        spinnerManager.bind(managerItems)
        spinnerPeregon1.bind(peregonItems)
        spinnerPeregon2.bind(peregonItems)

        setupSpinnerListenersForFallback(spinnerZubov, "зубов")
        setupSpinnerListenersForFallback(spinnerSkd, "скд")
        setupMachinistSpinnerListeners()
        setupManagerSpinnerListeners()
    }

    private fun setupSpinnerListenersForFallback(spinner: Spinner, type: String) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == spinner.adapter.count - 1) { // Если выбрана последняя опция ("Добавить...")
                    showAddWorkTypeDialog(type)
                    spinner.setSelection(EMPTY_POSITION) // Возвращаем к пустой позиции
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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

    private fun updateWorkTypeSpinners(type: String, spinner: Spinner) {
        lifecycleScope.launch {
            workTypeRepository.getWorkTypesByType(type).collect { workTypes ->
                val items = mutableListOf("") // Пустая строка
                items.addAll(workTypes.map { it.name })
                items.add("Добавить...") // Опция добавления

                val adapter = ArrayAdapter(this@WorkDayActivity, android.R.layout.simple_spinner_item, items)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                // Сохраняем текущую позицию
                val currentPosition = spinner.selectedItemPosition

                // Восстанавливаем позицию, если она валидна
                if (currentPosition < items.size) {
                    spinner.setSelection(currentPosition)
                }

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        if (position == items.size - 1) { // Если выбрана последняя опция ("Добавить...")
                            showAddWorkTypeDialog(type)
                            spinner.setSelection(EMPTY_POSITION) // Возвращаем к пустой позиции
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                    // После добавления обновим спиннеры
                    updateWorkTypeSpinners(type, if (type == "зубов") spinnerZubov else spinnerSkd)
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }
}