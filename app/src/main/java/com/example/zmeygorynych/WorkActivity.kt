package com.example.zmeygorynych

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class WorkActivity : BaseActivity() {

    private lateinit var customCalendarView: CustomCalendarView
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvCalendarTitle: TextView
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val monthNames = arrayOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )

    override fun getLayoutResourceId(): Int = R.layout.activity_work

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Настройка заголовка
        supportActionBar?.title = "Работа"

        // Инициализация views
        customCalendarView = findViewById(R.id.customCalendarView)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvCalendarTitle = findViewById(R.id.tvCalendarTitle)

        // Установка текущей даты
        val today = Calendar.getInstance()
        updateSelectedDate(today.time)
        updateCalendarTitle(today.get(Calendar.YEAR), today.get(Calendar.MONTH))

        // Обработчик выбора даты
        customCalendarView.setOnDateSelectedListener { year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            updateSelectedDate(selectedDate.time)
            // Обновляем подсветку выбранной даты
            customCalendarView.generateCalendar()
        }

        // Обработчик изменения месяца/года в календаре
        customCalendarView.setOnMonthYearChangedListener { year, month ->
            updateCalendarTitle(year, month)
        }

        // Обработчик нажатия на заголовок календаря
        tvCalendarTitle.setOnClickListener {
            showDatePickerDialog()
        }

        // Обработчик кнопки "Открыть меню"
        val btnOpenMenu = findViewById<Button>(R.id.btnOpenMenu)
        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Обновляем цвета календаря при возврате в активность
        updateCalendarColors()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

        private fun updateSelectedDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val monthName = monthNames[month]
        tvSelectedDate.text = "$day $monthName $year года"
    }

        private fun showDatePickerDialog() {
        // Получаем текущую дату из календаря
        val currentMonth = customCalendarView.getCurrentMonth()
        val currentYear = customCalendarView.getCurrentYear()

        val dialog = DatePickerDialog(
            this,
            currentMonth,
            currentYear
        ) { month, year ->
            // Обновляем календарь с выбранным месяцем и годом
            customCalendarView.setDate(year, month, 1)

            // Обновляем отображение выбранной даты
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, 1)
            updateSelectedDate(selectedDate.time)
        }

        dialog.show()
    }

    private fun updateCalendarTitle(year: Int, month: Int) {
        val monthName = monthNames[month]
        tvCalendarTitle.text = "$monthName $year"
    }

    private fun updateCalendarColors() {
        // Обновляем цвета календаря при изменении настроек
        customCalendarView.updateColors()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем цвета календаря при возврате в активность
        updateCalendarColors()
    }
}