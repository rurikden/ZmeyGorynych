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
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.TranslateAnimation

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private lateinit var gridLayout: GridLayout
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
    private var onDateSelectedListener: ((Int, Int, Int) -> Unit)? = null
    private var onMonthYearChangedListener: ((Int, Int) -> Unit)? = null
    private var selectedDate: Calendar = Calendar.getInstance()
    private var currentSelectedDate: Calendar = Calendar.getInstance()
    private var appSettings: AppSettings? = null
    private var highlightedDates: Set<Date> = emptySet()

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        goToPreviousMonthWithAnimation()
                    } else {
                        goToNextMonthWithAnimation()
                    }
                    return true
                }
            }
            return false
        }
    })

    init {
        setupView()
        // Инициализируем настройки
        try {
            appSettings = AppSettings.getInstance(context)
        } catch (e: Exception) {
            // Если настройки недоступны, используем цвета по умолчанию
        }
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

    fun generateCalendar() {
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
            val cellCalendar = calendar.clone() as Calendar
            cellCalendar.set(Calendar.DAY_OF_MONTH, day)
            addDateCell(day, cellCalendar)
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

            // Проверяем, является ли день сегодняшним
            val today = Calendar.getInstance()
            val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                         monthCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                         monthCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

            // Проверяем, является ли день выбранным
            val isSelected = day == selectedDate.get(Calendar.DAY_OF_MONTH) &&
                           monthCalendar.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                           monthCalendar.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)

            // Проверяем, является ли день выделенным (есть данные)
            val isHighlighted = highlightedDates.any { highlightedDate ->
                val cal = Calendar.getInstance()
                cal.time = highlightedDate
                day == cal.get(Calendar.DAY_OF_MONTH) &&
                monthCalendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                monthCalendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
            }

            // Проверяем, является ли день выходным
            val dayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

            // Устанавливаем цвет текста в порядке приоритета (снизу вверх)
            when {
                isSelected -> {
                    val selectedColor = appSettings?.selectedDayColor ?: Color.parseColor("#9C27B0")
                    setTextColor(selectedColor)
                    setBackgroundColor(Color.TRANSPARENT)
                }
                isToday -> {
                    val currentColor = appSettings?.currentDayColor ?: Color.parseColor("#00BCD4")
                    setTextColor(currentColor)
                    setBackgroundColor(Color.TRANSPARENT)
                }
                isHighlighted -> {
                    setTextColor(Color.parseColor("#4CAF50")) // Зеленый для выделенных дат
                    setBackgroundColor(Color.TRANSPARENT)
                }
                isWeekend -> {
                    val weekendColor = appSettings?.weekendColor ?: Color.RED
                    setTextColor(weekendColor)
                    setBackgroundColor(Color.TRANSPARENT)
                }
                else -> {
                    setTextColor(Color.BLACK) // Обычный черный - низший приоритет
                    setBackgroundColor(Color.TRANSPARENT)
                }
            }

            setOnClickListener {
                // Виброотклик при выборе даты
                performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                // Сохраняем выбранную дату, но НЕ меняем месяц календаря
                selectedDate = monthCalendar.clone() as Calendar
                selectedDate.set(Calendar.DAY_OF_MONTH, day)

                // Уведомляем о выборе даты
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

    fun setOnMonthYearChangedListener(listener: (Int, Int) -> Unit) {
        onMonthYearChangedListener = listener
    }

    fun getCurrentMonth(): Int {
        return calendar.get(Calendar.MONTH)
    }

    fun getCurrentYear(): Int {
        return calendar.get(Calendar.YEAR)
    }

    fun setDate(year: Int, month: Int, day: Int) {
        calendar.set(year, month, day)
        generateCalendar()
        // Уведомляем об изменении месяца и года
        onMonthYearChangedListener?.invoke(year, month)
    }

    fun updateColors() {
        // Обновляем цвета календаря при изменении настроек
        generateCalendar()
    }

    fun setHighlightedDates(dates: Set<Date>) {
        highlightedDates = dates
        generateCalendar()
    }

    private fun updateDateCellBackground(dateCell: TextView, day: Int, monthCalendar: Calendar) {
        // Проверяем, является ли день сегодняшним
        val today = Calendar.getInstance()
        val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                     monthCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                     monthCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)

                // Проверяем, является ли день выходным
        val dayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        // Устанавливаем цвет текста в порядке приоритета (снизу вверх)
        when {
            isToday -> {
                dateCell.setTextColor(Color.parseColor("#00BCD4")) // Бирюзовый - высший приоритет
                dateCell.setBackgroundColor(Color.TRANSPARENT)
            }
            isWeekend -> {
                dateCell.setTextColor(Color.RED) // Красный
                dateCell.setBackgroundColor(Color.TRANSPARENT)
            }
            else -> {
                dateCell.setTextColor(Color.BLACK) // Обычный черный - низший приоритет
                dateCell.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    private fun updateAllDateCells() {
        // Проходим по всем дочерним элементам GridLayout
        for (i in 7 until gridLayout.childCount) { // Пропускаем заголовки дней недели
            val child = gridLayout.getChildAt(i)
            if (child is TextView && child.text.isNotEmpty()) {
                val day = child.text.toString().toIntOrNull()
                if (day != null) {
                    val monthCalendar = calendar.clone() as Calendar
                    monthCalendar.set(Calendar.DAY_OF_MONTH, day)
                    updateDateCellBackground(child, day, monthCalendar)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    private fun goToNextMonthWithAnimation() {
        val oldMonth = calendar.clone() as Calendar
        calendar.add(Calendar.MONTH, 1)
        animateMonthChange(true) // true = next
        onMonthYearChangedListener?.invoke(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        generateCalendar()
    }

    private fun goToPreviousMonthWithAnimation() {
        val oldMonth = calendar.clone() as Calendar
        calendar.add(Calendar.MONTH, -1)
        animateMonthChange(false) // false = previous
        onMonthYearChangedListener?.invoke(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
        generateCalendar()
    }

    private fun animateMonthChange(isNext: Boolean) {
        val fromX = if (isNext) gridLayout.width.toFloat() else -gridLayout.width.toFloat()
        val toX = 0f
        val anim = TranslateAnimation(fromX, toX, 0f, 0f)
        anim.duration = 250
        gridLayout.startAnimation(anim)
    }
}