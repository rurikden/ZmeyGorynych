package com.example.zmeygorynych

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import kotlin.math.*

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
        val colorWheel = view.findViewById<ColorWheelView>(R.id.colorWheel)
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
        colorWheel.setOnColorChangedListener { hue, saturation ->
            val value = valueSlider.value / 100f
            val newColor = Color.HSVToColor(floatArrayOf(hue, saturation, value))
            colorPreview.setBackgroundColor(newColor)
            currentColor = newColor

            // Обновляем слайдеры
            hueSlider.value = hue
            saturationSlider.value = saturation * 100
        }

        // Обновляем цвет при изменении слайдеров
        val updateColor = {
            val hue = hueSlider.value
            val saturation = saturationSlider.value / 100f
            val value = valueSlider.value / 100f
            val newColor = Color.HSVToColor(floatArrayOf(hue, saturation, value))
            colorPreview.setBackgroundColor(newColor)
            currentColor = newColor

            // Обновляем ColorWheel
            colorWheel.setHueSaturation(hue, saturation)
        }

        hueSlider.addOnChangeListener { _, _, _ -> updateColor() }
        saturationSlider.addOnChangeListener { _, _, _ -> updateColor() }
        valueSlider.addOnChangeListener { _, _, _ -> updateColor() }

        // Устанавливаем начальные значения в ColorWheel
        colorWheel.setHueSaturation(hsv[0], hsv[1])

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

// Кастомное представление для цветового круга
class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var currentHue = 0f
    private var currentSaturation = 1f
    private var onColorChangedListener: ((Float, Float) -> Unit)? = null

    init {
        centerPaint.color = Color.WHITE
        centerPaint.style = Paint.Style.FILL

        borderPaint.color = Color.GRAY
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 4f

        selectorPaint.color = Color.WHITE
        selectorPaint.style = Paint.Style.FILL
        selectorPaint.strokeWidth = 4f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f - 20f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисуем цветовой круг
        val shader = SweepGradient(centerX, centerY, createHueColors(), null)
        paint.shader = shader
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Рисуем градиент насыщенности
        val saturationShader = RadialGradient(
            centerX, centerY, radius,
            Color.WHITE, Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = saturationShader
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Рисуем границу
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        // Рисуем селектор
        val angle = currentHue * PI / 180f
        val distance = currentSaturation * radius
        val selectorX = centerX + (distance * cos(angle)).toFloat()
        val selectorY = centerY + (distance * sin(angle)).toFloat()

        selectorPaint.color = Color.BLACK
        canvas.drawCircle(selectorX, selectorY, 12f, selectorPaint)
        selectorPaint.color = Color.WHITE
        canvas.drawCircle(selectorX, selectorY, 8f, selectorPaint)
    }

    private fun createHueColors(): IntArray {
        val colors = IntArray(361)
        for (i in 0..360) {
            val hsv = FloatArray(3)
            hsv[0] = i.toFloat()
            hsv[1] = 1f
            hsv[2] = 1f
            colors[i] = Color.HSVToColor(hsv)
        }
        return colors
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x - centerX
                val y = event.y - centerY
                val distance = sqrt(x * x + y * y)

                if (distance <= radius) {
                    // Вычисляем угол (оттенок)
                    val angle = atan2(y, x) * 180f / PI.toFloat()
                    currentHue = if (angle < 0) angle + 360f else angle

                    // Вычисляем расстояние (насыщенность)
                    currentSaturation = (distance / radius).coerceIn(0f, 1f)

                    onColorChangedListener?.invoke(currentHue, currentSaturation)
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setOnColorChangedListener(listener: (Float, Float) -> Unit) {
        onColorChangedListener = listener
    }

    fun setHueSaturation(hue: Float, saturation: Float) {
        currentHue = hue
        currentSaturation = saturation
        invalidate()
    }
}