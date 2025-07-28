package com.example.zmeygorynych

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.cardview.widget.CardView
import java.util.*

class DatePickerDialog(
    context: Context,
    private val currentMonth: Int,
    private val currentYear: Int,
    private val onDateSelected: (Int, Int) -> Unit
) : Dialog(context, R.style.FullScreenDialog) {

    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    private lateinit var btnOk: Button
    private lateinit var btnCancel: Button
    private lateinit var dialogCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_date_picker)

        // Инициализация views
        monthPicker = findViewById(R.id.monthPicker)
        yearPicker = findViewById(R.id.yearPicker)
        btnOk = findViewById(R.id.btnOk)
        btnCancel = findViewById(R.id.btnCancel)
        dialogCard = findViewById(R.id.dialogCard)

        setupNumberPickers()
        setupButtons()
        setupAnimations()
    }

    private fun setupNumberPickers() {
        // Настройка NumberPicker для месяцев
        val months = arrayOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
        monthPicker.apply {
            displayedValues = months
            minValue = 0
            maxValue = 11
            value = currentMonth
            wrapSelectorWheel = false
        }

        // Настройка NumberPicker для годов
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.apply {
            minValue = currentYear - 5
            maxValue = currentYear + 5
            value = currentYear
            wrapSelectorWheel = false
        }

        // Добавляем haptic feedback
        monthPicker.setOnValueChangedListener { _, _, _ ->
            performHapticFeedback()
        }
        yearPicker.setOnValueChangedListener { _, _, _ ->
            performHapticFeedback()
        }
    }

    private fun setupButtons() {
        btnOk.setOnClickListener {
            val selectedMonth = monthPicker.value
            val selectedYear = yearPicker.value
            onDateSelected(selectedMonth, selectedYear)
            dismissWithAnimation()
        }

        btnCancel.setOnClickListener {
            dismissWithAnimation()
        }
    }

    private fun setupAnimations() {
        // Анимация появления
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        val scaleIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)

        dialogCard.startAnimation(fadeIn)
    }

    private fun dismissWithAnimation() {
        val fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        dialogCard.startAnimation(fadeOut)

        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                dismiss()
            }
        })
    }

    private fun performHapticFeedback() {
        dialogCard.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
    }
}