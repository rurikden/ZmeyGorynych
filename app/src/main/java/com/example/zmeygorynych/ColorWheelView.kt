package com.example.zmeygorynych

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class ColorWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnColorChangedListener {
        fun onColorChanged(color: Int)
    }

    // Параметры для настройки внешнего вида
    companion object {
        private const val BORDER_WIDTH = 3f
        private const val INDICATOR_RADIUS = 20f
        private const val INDICATOR_INNER_RADIUS = 16f
        private const val INDICATOR_CENTER_RADIUS = 4f
        private const val SHADOW_RADIUS = 10f
        private const val SHADOW_DX = 0f
        private const val SHADOW_DY = 5f
        private const val SHADOW_ALPHA = 100
    }

    private var listener: OnColorChangedListener? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var selectedColor = Color.RED
    private var isShadowEnabled = true

    init {
        setupPaints()
    }

    private fun setupPaints() {
        // Основная краска для цветового круга
        paint.style = Paint.Style.FILL

        // Краска для белой границы
        borderPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = BORDER_WIDTH
        }

        // Краска для тени
        shadowPaint.apply {
            color = Color.TRANSPARENT
            setShadowLayer(
                SHADOW_RADIUS,
                SHADOW_DX,
                SHADOW_DY,
                Color.argb(SHADOW_ALPHA, 0, 0, 0)
            )
        }

        // Краски для указателя
        indicatorOuterPaint.apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        indicatorInnerPaint.apply {
            style = Paint.Style.FILL
        }

        indicatorCenterPaint.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
    }

    fun setOnColorChangedListener(listener: OnColorChangedListener) {
        this.listener = listener
    }

    fun setColor(color: Int) {
        selectedColor = color
        invalidate()
    }

    fun enableShadow(enabled: Boolean) {
        isShadowEnabled = enabled
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldw)
        centerX = w / 2f
        centerY = h / 2f
        radius = (min(w, h) / 2f) * 0.9f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Очищаем фон
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // Включаем слой для тени если нужно
        if (isShadowEnabled) {
            setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint)
            // Рисуем тень
            canvas.drawCircle(centerX, centerY, radius, shadowPaint)
        }

        // Рисуем цветовой круг (радужный градиент)
        val colors = createRainbowColors()
        val shader = SweepGradient(centerX, centerY, colors, null)
        paint.shader = shader
        canvas.drawCircle(centerX, centerY, radius, paint)

        // Добавляем белую границу для четкости
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        // Рисуем указатель выбранного цвета
        drawColorIndicator(canvas)

        // Возвращаем аппаратное ускорение
        if (isShadowEnabled) {
            setLayerType(LAYER_TYPE_HARDWARE, null)
        }
    }

    private fun drawColorIndicator(canvas: Canvas) {
        val hsv = FloatArray(3)
        Color.colorToHSV(selectedColor, hsv)
        val angle = Math.toRadians(hsv[0].toDouble())
        val selectRadius = radius * hsv[1].coerceIn(0f, 1f)
        val selectX = centerX + (selectRadius * cos(angle)).toFloat()
        val selectY = centerY + (selectRadius * sin(angle)).toFloat()

        // Внешнее белое кольцо указателя
        canvas.drawCircle(selectX, selectY, INDICATOR_RADIUS, indicatorOuterPaint)

        // Внутренняя цветная часть указателя
        indicatorInnerPaint.color = selectedColor
        canvas.drawCircle(selectX, selectY, INDICATOR_INNER_RADIUS, indicatorInnerPaint)

        // Черная точка в центре для контраста
        canvas.drawCircle(selectX, selectY, INDICATOR_CENTER_RADIUS, indicatorCenterPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val x = event.x - centerX
                val y = event.y - centerY
                val distance = sqrt(x * x + y * y)

                if (distance <= radius) {
                    val angle = atan2(y, x).toFloat() * (180f / PI.toFloat())
                    val hue = if (angle < 0) angle + 360f else angle
                    val saturation = (distance / radius).coerceIn(0f, 1f)

                    selectedColor = Color.HSVToColor(floatArrayOf(hue, saturation, 1f))
                    invalidate()
                    listener?.onColorChanged(selectedColor)
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                // Можно добавить обработку окончания выбора
                listener?.onColorChanged(selectedColor)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun createRainbowColors(): IntArray {
        // Создаем плавный переход через все цвета радуги
        return IntArray(361) { i ->
            Color.HSVToColor(floatArrayOf(i.toFloat(), 1f, 1f))
        }
    }

    // Дополнительные методы для удобства работы

    fun getCurrentColor(): Int = selectedColor

    fun getCurrentHSV(): FloatArray {
        val hsv = FloatArray(3)
        Color.colorToHSV(selectedColor, hsv)
        return hsv
    }

    fun setHSV(hue: Float, saturation: Float, value: Float = 1f) {
        selectedColor = Color.HSVToColor(floatArrayOf(
            hue.coerceIn(0f, 360f),
            saturation.coerceIn(0f, 1f),
            value.coerceIn(0f, 1f)
        ))
        invalidate()
    }

    // Метод для создания цветового круга с прозрачным центром (опция)
    private fun createRadialColorsWithTransparency(): IntArray {
        return intArrayOf(
            Color.TRANSPARENT,
            Color.HSVToColor(150, floatArrayOf(0f, 1f, 1f)),
            Color.HSVToColor(150, floatArrayOf(120f, 1f, 1f)),
            Color.HSVToColor(150, floatArrayOf(240f, 1f, 1f)),
            Color.HSVToColor(150, floatArrayOf(360f, 1f, 1f))
        )
    }

    // Метод для отрисовки с прозрачным центром (раскомментировать если нужно)
    private fun drawWithTransparentCenter(canvas: Canvas) {
        val radialColors = createRadialColorsWithTransparency()
        val positions = floatArrayOf(0f, 0.2f, 0.5f, 0.8f, 1f)
        val radialShader = RadialGradient(
            centerX, centerY, radius,
            radialColors, positions, Shader.TileMode.CLAMP
        )
        paint.shader = radialShader
        canvas.drawCircle(centerX, centerY, radius, paint)
    }
}