package com.example.zmeygorynych

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider

class ColorPickerDialog : DialogFragment() {

    private var onColorSelectedListener: ((Int) -> Unit)? = null
    private var currentColor: Int = Color.RED
    private var title: String = "Выберите цвет"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_color_picker, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvColorPickerTitle)
        val colorPreview = view.findViewById<View>(R.id.colorPreview)
        val colorWheel = view.findViewById<View>(R.id.colorWheel)

        val hueSlider = view.findViewById<Slider>(R.id.hueSlider)
        val saturationSlider = view.findViewById<Slider>(R.id.saturationSlider)
        val valueSlider = view.findViewById<Slider>(R.id.valueSlider)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = title
        colorPreview.setBackgroundColor(currentColor)

        // Устанавливаем начальные значения слайдеров
        val hsv = FloatArray(3)
        Color.colorToHSV(currentColor, hsv)
        hueSlider.value = hsv[0]
        saturationSlider.value = hsv[1] * 100
        valueSlider.value = hsv[2] * 100

        // Настройка ColorWheel
        if (colorWheel is ColorWheelView) {
            colorWheel.setOnColorChangedListener(object : ColorWheelView.OnColorChangedListener {
                override fun onColorChanged(color: Int) {
                    currentColor = color
                    colorPreview.setBackgroundColor(color)

                    // Обновляем слайдеры
                    Color.colorToHSV(color, hsv)
                    hueSlider.value = hsv[0]
                    saturationSlider.value = hsv[1] * 100
                    valueSlider.value = hsv[2] * 100
                }
            })
            colorWheel.setColor(currentColor)
        }

        // Обработчики слайдеров
        hueSlider.addOnChangeListener { _, value, _ ->
            hsv[0] = value
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
            if (colorWheel is ColorWheelView) {
                (colorWheel as ColorWheelView).setColor(currentColor)
            }
        }

        saturationSlider.addOnChangeListener { _, value, _ ->
            hsv[1] = value / 100f
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
            if (colorWheel is ColorWheelView) {
                (colorWheel as ColorWheelView).setColor(currentColor)
            }
        }

        valueSlider.addOnChangeListener { _, value, _ ->
            hsv[2] = value / 100f
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
            if (colorWheel is ColorWheelView) {
                (colorWheel as ColorWheelView).setColor(currentColor)
            }
        }

        btnOk.setOnClickListener {
            onColorSelectedListener?.invoke(currentColor)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        builder.setView(view)

        // Создаем диалог
        val dialog = builder.create()

        // Устанавливаем полноэкранный режим
        val window = dialog.window
        if (window != null) {
            // Полноэкранный режим
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

            // Прозрачный фон
            window.setBackgroundDrawableResource(android.R.color.transparent)

            // Анимация (опционально)
            //window.setWindowAnimations(R.style.DialogAnimation)
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        // Дополнительная настройка при старте
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    companion object {
        fun newInstance(title: String, initialColor: Int, listener: (Int) -> Unit): ColorPickerDialog {
            return ColorPickerDialog().apply {
                this.title = title
                this.currentColor = initialColor
                this.onColorSelectedListener = listener
            }
        }
    }
}