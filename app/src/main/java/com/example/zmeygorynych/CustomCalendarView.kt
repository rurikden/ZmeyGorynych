package com.example.zmeygorynych

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private lateinit var gridLayout: GridLayout
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
    private var onDateSelectedListener: ((Int, Int, Int) -> Unit)? = null
    private var selectedDate: Calendar = Calendar.getInstance()

    init {
        setupView()
    }

    private fun setupView() {
        // Создаем GridLayout для календаря
        gridLayout = GridLayout(context).apply {
            columnCount = 7
            rowCount = 7 // 1 строка для дней недели + 6 строк для дат
        }

        addView(gridLayout)

        // Добавляем заголовки дней недели
        addDayHeaders()

        // Генерируем календарь для текущего месяца
        generateCalendar()
    }

    private fun addDayHeaders() {
        val daysOfWeek = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

        for (day in daysOfWeek) {
            val dayHeader = TextView(context).apply {
                text = day
                textSize = 14f
                setTextColor(Color.parseColor("#6200EE"))
                setPadding(16, 16, 16, 16)
                gravity = android.view.Gravity.CENTER
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }

            gridLayout.addView(dayHeader, params)
        }
    }

    private fun generateCalendar() {
        // Очищаем предыдущие даты (кроме заголовков)
        for (i in gridLayout.childCount - 1 downTo 7) {
            gridLayout.removeViewAt(i)
        }

        val currentMonth = calendar.clone() as Calendar
        currentMonth.set(Calendar.DAY_OF_MONTH, 1)

        // Добавляем пустые ячейки до первого дня месяца
        val firstDayOfWeek = currentMonth.get(Calendar.DAY_OF_WEEK)
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2

        for (i in 0 until offset) {
            addEmptyCell()
        }

        // Добавляем даты месяца
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            addDateCell(day, currentMonth)
            currentMonth.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun addEmptyCell() {
        val emptyCell = TextView(context).apply {
            text = ""
            setPadding(16, 16, 16, 16)
        }

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }

        gridLayout.addView(emptyCell, params)
    }

    private fun addDateCell(day: Int, monthCalendar: Calendar) {
        val dateCell = TextView(context).apply {
            text = day.toString()
            textSize = 16f
            setPadding(16, 16, 16, 16)
            gravity = android.view.Gravity.CENTER

            // Проверяем, является ли день выходным
            val dayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

            if (isWeekend) {
                setTextColor(Color.RED)
            } else {
                setTextColor(Color.BLACK)
            }

            // Проверяем, является ли день сегодняшним
            val today = Calendar.getInstance()
            val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                         monthCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                         monthCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

            if (isToday) {
                setBackgroundColor(Color.parseColor("#E8F5E8"))
            }

            setOnClickListener {
                selectedDate = monthCalendar.clone() as Calendar
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                onDateSelectedListener?.invoke(
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
                )
            }
        }

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }

        gridLayout.addView(dateCell, params)
    }

    fun setOnDateSelectedListener(listener: (Int, Int, Int) -> Unit) {
        onDateSelectedListener = listener
    }

    fun setDate(year: Int, month: Int, day: Int) {
        calendar.set(year, month, day)
        generateCalendar()
    }
}