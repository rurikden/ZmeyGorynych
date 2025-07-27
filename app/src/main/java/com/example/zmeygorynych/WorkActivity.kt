package com.example.zmeygorynych

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class WorkActivity : AppCompatActivity() {

    private lateinit var customCalendarView: CustomCalendarView
    private lateinit var tvSelectedDate: TextView
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_work)

        // Инициализация views
        customCalendarView = findViewById(R.id.customCalendarView)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)

        // Установка текущей даты
        val today = Calendar.getInstance()
        updateSelectedDate(today.time)

        // Обработчик выбора даты
        customCalendarView.setOnDateSelectedListener { year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            updateSelectedDate(selectedDate.time)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

        private fun updateSelectedDate(date: Date) {
        val dateString = dateFormat.format(date)
        tvSelectedDate.text = "Выбранная дата: $dateString"
    }
}