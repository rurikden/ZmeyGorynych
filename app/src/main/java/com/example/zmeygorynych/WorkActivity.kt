package com.example.zmeygorynych

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkActivity : BaseActivity() {

    private lateinit var customCalendarView: CustomCalendarView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvWorkDayInfo: TextView
    private lateinit var btnWorkDay: Button

    private lateinit var workDayRepository: WorkDayRepository

    // Текущая выбранная дата
    private lateinit var selectedDate: Date

    override fun getLayoutResourceId(): Int = R.layout.activity_work

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Настройка заголовка
        supportActionBar?.title = "Работа"

        // Инициализация базы данных
        val database = AppDatabase.getDatabase(this)
        workDayRepository = WorkDayRepository(database.workDayDao())

        // Инициализация views
        customCalendarView = findViewById(R.id.customCalendarView)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvWorkDayInfo = findViewById(R.id.tvWorkDayInfo)
        btnWorkDay = findViewById(R.id.btnWorkDay)

        // Установка текущей даты
        val today = Calendar.getInstance()
        selectedDate = today.time
        updateSelectedDate(selectedDate)

        // Загрузка данных за выбранную дату
        loadWorkDayInfo(selectedDate)

        // Обработчик выбора даты
        customCalendarView.setOnDateSelectedListener { year, month, dayOfMonth ->
            val newSelectedDate = Calendar.getInstance()
            newSelectedDate.set(year, month, dayOfMonth)
            selectedDate = newSelectedDate.time
            updateSelectedDate(selectedDate)
            loadWorkDayInfo(selectedDate)
            // Обновляем подсветку выбранной даты
            customCalendarView.generateCalendar()
        }

        // Обработчик изменения месяца/года в календаре
        customCalendarView.setOnMonthYearChangedListener { year, month ->
            // Обновление заголовка больше не нужно
        }

        // Обработчик кнопки "Рабочий день"
        btnWorkDay.setOnClickListener {
            val intent = Intent(this, WorkDayActivity::class.java).apply {
                putExtra("selected_date", selectedDate)
            }
            startActivity(intent)
        }

        // Настройка выделения дат в календаре
        setupCalendarHighlighting()

        // Обновляем цвета календаря при возврате в активность
        updateCalendarColors()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadWorkDayInfo(date: Date) {
        lifecycleScope.launch {
            try {
                val workDay = workDayRepository.getWorkDayByDate(date.time)
                if (workDay != null) {
                    val info = buildString {
                        workDay.zubovWorkType?.let { append("Зубов: $it\n") }
                        workDay.skdWorkType?.let { append("СКД: $it\n") }
                        workDay.machinist1?.let { append("Машинист 1: $it\n") }
                        workDay.machinist2?.let { append("Машинист 2: $it\n") }
                        workDay.manager?.let { append("Руководитель: $it\n") }
                        workDay.peregon1?.let { append("Перегон 1: $it\n") }
                        workDay.peregon2?.let { append("Перегон 2: $it\n") }

                        // Время работы
                        val timeInfo = mutableListOf<String>()
                        if (workDay.windowFrom1 != null && workDay.windowTo1 != null) {
                            timeInfo.add("Окно: ${workDay.windowFrom1}-${workDay.windowTo1}")
                        }
                        if (workDay.windowFrom2 != null && workDay.windowTo2 != null) {
                            timeInfo.add("Окно 2: ${workDay.windowFrom2}-${workDay.windowTo2}")
                        }
                        if (workDay.skdFrom1 != null && workDay.skdTo1 != null) {
                            timeInfo.add("СКД: ${workDay.skdFrom1}-${workDay.skdTo1}")
                        }
                        if (workDay.skdFrom2 != null && workDay.skdTo2 != null) {
                            timeInfo.add("СКД 2: ${workDay.skdFrom2}-${workDay.skdTo2}")
                        }
                        if (workDay.zubovFrom1 != null && workDay.zubovTo1 != null) {
                            timeInfo.add("Зубов: ${workDay.zubovFrom1}-${workDay.zubovTo1}")
                        }
                        if (workDay.zubovFrom2 != null && workDay.zubovTo2 != null) {
                            timeInfo.add("Зубов 2: ${workDay.zubovFrom2}-${workDay.zubovTo2}")
                        }
                        if (timeInfo.isNotEmpty()) {
                            append("Время работы: ${timeInfo.joinToString(", ")}")
                        }
                    }
                    tvWorkDayInfo.text = info.trim()
                } else {
                    tvWorkDayInfo.text = "Нет данных за выбранную дату"
                }
            } catch (e: Exception) {
                tvWorkDayInfo.text = "Ошибка загрузки данных"
            }
        }
    }

    private fun setupCalendarHighlighting() {
        // Получаем все даты с записями и передаем в календарь для выделения
        lifecycleScope.launch {
            try {
                workDayRepository.getAllWorkDayDates().collect { timestamps ->
                    val dates = timestamps.map { Date(it) }.toSet()
                    customCalendarView.setHighlightedDates(dates)
                    customCalendarView.generateCalendar()
                }
            } catch (e: Exception) {
                // В случае ошибки ничего не делаем
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

    private fun updateCalendarColors() {
        // Обновляем цвета календаря при изменении настроек
        customCalendarView.updateColors()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем цвета календаря при возврате в активность
        updateCalendarColors()

        // Обновляем данные за выбранную дату (на случай если были изменения)
        loadWorkDayInfo(selectedDate)

        // Обновляем выделение дат
        setupCalendarHighlighting()
    }
}