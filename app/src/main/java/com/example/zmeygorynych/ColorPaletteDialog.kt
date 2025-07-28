package com.example.zmeygorynych

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider

class ColorPaletteDialog : DialogFragment() {

    private var onColorSelectedListener: ((Int) -> Unit)? = null
    private var currentColor: Int = Color.RED
    private var title: String = "Выберите цвет"

    // Предустановленные цвета
    private val predefinedColors = listOf(
        Color.RED, Color.parseColor("#FFE91E63"), Color.parseColor("#FF9C27B0"), Color.parseColor("#FF673AB7"),
        Color.parseColor("#FF3F51B5"), Color.parseColor("#FF2196F3"), Color.parseColor("#FF03A9F4"), Color.parseColor("#FF00BCD4"),
        Color.parseColor("#FF009688"), Color.parseColor("#FF4CAF50"), Color.parseColor("#FF8BC34A"), Color.parseColor("#FFCDDC39"),
        Color.parseColor("#FFFFEB3B"), Color.parseColor("#FFFFC107"), Color.parseColor("#FFFF9800"), Color.parseColor("#FFFF5722"),
        Color.parseColor("#FF795548"), Color.parseColor("#FF9E9E9E"), Color.parseColor("#FF607D8B"), Color.BLACK
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_color_palette, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvColorPaletteTitle)
        val colorPreview = view.findViewById<View>(R.id.colorPreview)
        val colorPalette = view.findViewById<GridLayout>(R.id.colorPalette)
        val hueSlider = view.findViewById<Slider>(R.id.hueSlider)
        val saturationSlider = view.findViewById<Slider>(R.id.saturationSlider)
        val valueSlider = view.findViewById<Slider>(R.id.valueSlider)
        val btnOk = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        tvTitle.text = title
        colorPreview.setBackgroundColor(currentColor)

        // Создаем цветовую палитру
        createColorPalette(colorPalette, colorPreview, hueSlider, saturationSlider, valueSlider)

        // Устанавливаем начальные значения слайдеров
        val hsv = FloatArray(3)
        Color.colorToHSV(currentColor, hsv)
        hueSlider.value = hsv[0]
        saturationSlider.value = hsv[1] * 100
        valueSlider.value = hsv[2] * 100

        // Обновляем цвет при изменении слайдеров
        val updateColor = {
            val hue = hueSlider.value
            val saturation = saturationSlider.value / 100f
            val value = valueSlider.value / 100f
            val newColor = Color.HSVToColor(floatArrayOf(hue, saturation, value))
            colorPreview.setBackgroundColor(newColor)
            currentColor = newColor
        }

        hueSlider.addOnChangeListener { _, _, _ -> updateColor() }
        saturationSlider.addOnChangeListener { _, _, _ -> updateColor() }
        valueSlider.addOnChangeListener { _, _, _ -> updateColor() }

        btnOk.setOnClickListener {
            onColorSelectedListener?.invoke(currentColor)
            dismiss()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    private fun createColorPalette(
        colorPalette: GridLayout,
        colorPreview: View,
        hueSlider: Slider,
        saturationSlider: Slider,
        valueSlider: Slider
    ) {
        val cellSize = resources.getDimensionPixelSize(R.dimen.color_cell_size)
        val margin = resources.getDimensionPixelSize(R.dimen.color_cell_margin)

        for (color in predefinedColors) {
            val colorButton = View(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    setMargins(margin, margin, margin, margin)
                }
                background = createColorBackground(color)

                setOnClickListener {
                    // Устанавливаем выбранный цвет
                    currentColor = color
                    colorPreview.setBackgroundColor(color)

                    // Обновляем слайдеры
                    val hsv = FloatArray(3)
                    Color.colorToHSV(color, hsv)
                    hueSlider.value = hsv[0]
                    saturationSlider.value = hsv[1] * 100
                    valueSlider.value = hsv[2] * 100
                }
            }

            colorPalette.addView(colorButton)
        }
    }

    private fun createColorBackground(color: Int): android.graphics.drawable.Drawable {
        val shape = android.graphics.drawable.GradientDrawable()
        shape.shape = android.graphics.drawable.GradientDrawable.RECTANGLE
        shape.cornerRadius = resources.getDimensionPixelSize(R.dimen.color_cell_corner_radius).toFloat()
        shape.setColor(color)
        shape.setStroke(
            resources.getDimensionPixelSize(R.dimen.color_cell_stroke_width),
            Color.GRAY
        )
        return shape
    }

    companion object {
        fun newInstance(title: String, initialColor: Int, listener: (Int) -> Unit): ColorPaletteDialog {
            return ColorPaletteDialog().apply {
                this.title = title
                this.currentColor = initialColor
                this.onColorSelectedListener = listener
            }
        }
    }
}