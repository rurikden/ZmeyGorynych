package com.example.zmeygorynych

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText


class SettingsActivity : BaseActivity() {

    private lateinit var appSettings: AppSettings
    private lateinit var selectedDayColorPreview: View
    private lateinit var currentDayColorPreview: View
    private lateinit var weekendColorPreview: View
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var settingsTabLayout: TabLayout
    private lateinit var appearanceTab: View
    private lateinit var systemTab: View
    private lateinit var spinnerColorPreview: View
    private lateinit var spinnerCornerRadiusSlider: Slider
    private lateinit var spinnerCornerRadiusValue: TextView
    private lateinit var backgroundColorPreview: View
    private lateinit var cardBackgroundColorPreview: View
    
    // Views для вкладки Топливо
    private lateinit var fuelTab: View
    private lateinit var summerMonthsChipGroup: ChipGroup
    private lateinit var etZubovSummerFuel: TextInputEditText
    private lateinit var etZubovWinterFuel: TextInputEditText
    private lateinit var etSkdSummerFuel: TextInputEditText
    private lateinit var etSkdWinterFuel: TextInputEditText
    
    // Названия месяцев
    private val monthNames = listOf(
        R.string.month_january,
        R.string.month_february,
        R.string.month_march,
        R.string.month_april,
        R.string.month_may,
        R.string.month_june,
        R.string.month_july,
        R.string.month_august,
        R.string.month_september,
        R.string.month_october,
        R.string.month_november,
        R.string.month_december
    )

    override fun getLayoutResourceId(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация настроек
        appSettings = AppSettings.getInstance(this)

        // Настройка заголовка
        supportActionBar?.title = "Настройки"

        // Инициализация views
        initializeViews()

        // Загрузка текущих настроек
        loadCurrentSettings()

        // Настройка вкладок
        setupTabs()

        // Настройка обработчиков
        setupListeners()

        // Настройка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        selectedDayColorPreview = findViewById(R.id.selectedDayColorPreview)
        currentDayColorPreview = findViewById(R.id.currentDayColorPreview)
        weekendColorPreview = findViewById(R.id.weekendColorPreview)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        settingsTabLayout = findViewById(R.id.settingsTabLayout)
        appearanceTab = findViewById(R.id.appearanceTab)
        systemTab = findViewById(R.id.systemTab)
        spinnerColorPreview = findViewById(R.id.spinnerColorPreview)
        spinnerCornerRadiusSlider = findViewById(R.id.spinnerCornerRadiusSlider)
        spinnerCornerRadiusValue = findViewById(R.id.spinnerCornerRadiusValue)
        backgroundColorPreview = findViewById(R.id.backgroundColorPreview)
        cardBackgroundColorPreview = findViewById(R.id.cardBackgroundColorPreview)
        
        // Инициализация views для вкладки Топливо
        fuelTab = findViewById(R.id.fuelTab)
        summerMonthsChipGroup = findViewById(R.id.summerMonthsChipGroup)
        etZubovSummerFuel = findViewById(R.id.etZubovSummerFuel)
        etZubovWinterFuel = findViewById(R.id.etZubovWinterFuel)
        etSkdSummerFuel = findViewById(R.id.etSkdSummerFuel)
        etSkdWinterFuel = findViewById(R.id.etSkdWinterFuel)
        
        // Инициализация месяцев в ChipGroup
        initializeMonthChips()
    }

    private fun setupTabs() {
        // Создаем вкладки
        settingsTabLayout.addTab(settingsTabLayout.newTab().setText(R.string.appearance_tab))
        settingsTabLayout.addTab(settingsTabLayout.newTab().setText(R.string.system_tab))
        settingsTabLayout.addTab(settingsTabLayout.newTab().setText(R.string.fuel_tab))

        // Выбираем первую вкладку по умолчанию
        settingsTabLayout.selectTab(settingsTabLayout.getTabAt(0))

        // Обработчик переключения вкладок
        settingsTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        appearanceTab.visibility = View.VISIBLE
                        systemTab.visibility = View.GONE
                        fuelTab.visibility = View.GONE
                    }
                    1 -> {
                        appearanceTab.visibility = View.GONE
                        systemTab.visibility = View.VISIBLE
                        fuelTab.visibility = View.GONE
                    }
                    2 -> {
                        appearanceTab.visibility = View.GONE
                        systemTab.visibility = View.GONE
                        fuelTab.visibility = View.VISIBLE
                        // Загружаем настройки топлива при открытии вкладки
                        loadFuelSettings()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadCurrentSettings() {
        // Загружаем цвета
        selectedDayColorPreview.setBackgroundColor(appSettings.selectedDayColor)
        currentDayColorPreview.setBackgroundColor(appSettings.currentDayColor)
        weekendColorPreview.setBackgroundColor(appSettings.weekendColor)

        // Загружаем настройки цветов интерфейса
        backgroundColorPreview.setBackgroundColor(appSettings.backgroundColor)
        cardBackgroundColorPreview.setBackgroundColor(appSettings.cardBackgroundColor)

        // Загружаем настройки спиннеров
        spinnerColorPreview.setBackgroundColor(appSettings.spinnerColor)
        spinnerCornerRadiusSlider.value = appSettings.spinnerCornerRadius.toFloat()
        spinnerCornerRadiusValue.text = "${appSettings.spinnerCornerRadius}dp"

        // Загружаем тему
        when (appSettings.themeMode) {
            AppSettings.ThemeMode.LIGHT -> themeRadioGroup.check(R.id.rbLightTheme)
            AppSettings.ThemeMode.DARK -> themeRadioGroup.check(R.id.rbDarkTheme)
            AppSettings.ThemeMode.SYSTEM -> themeRadioGroup.check(R.id.rbSystemTheme)
        }
    }

    private fun setupListeners() {
        // Обработчик кнопки "Открыть меню"
        val btnOpenMenu = findViewById<Button>(R.id.btnOpenMenu)
        btnOpenMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

                // Обработчики кнопок изменения цветов
        findViewById<Button>(R.id.btnSelectedDayColor).setOnClickListener {
            showColorPicker("Выберите цвет выделенного дня", appSettings.selectedDayColor) { color ->
                appSettings.selectedDayColor = color
                selectedDayColorPreview.setBackgroundColor(color)
                // Обновляем календарь в WorkActivity
                updateCalendarColors()
            }
        }

        findViewById<Button>(R.id.btnCurrentDayColor).setOnClickListener {
            showColorPicker("Выберите цвет текущего дня", appSettings.currentDayColor) { color ->
                appSettings.currentDayColor = color
                currentDayColorPreview.setBackgroundColor(color)
                // Обновляем календарь в WorkActivity
                updateCalendarColors()
            }
        }

        findViewById<Button>(R.id.btnWeekendColor).setOnClickListener {
            showColorPicker("Выберите цвет выходных", appSettings.weekendColor) { color ->
                appSettings.weekendColor = color
                weekendColorPreview.setBackgroundColor(color)
                // Обновляем календарь в WorkActivity
                updateCalendarColors()
            }
        }

        // Обработчики цветов интерфейса
        findViewById<Button>(R.id.btnBackgroundColor).setOnClickListener {
            showColorPicker("Выберите цвет фона", appSettings.backgroundColor) { color ->
                appSettings.backgroundColor = color
                backgroundColorPreview.setBackgroundColor(color)
                applyBackgroundColor()
            }
        }

        findViewById<Button>(R.id.btnCardBackgroundColor).setOnClickListener {
            showColorPicker("Выберите цвет карточек", appSettings.cardBackgroundColor) { color ->
                appSettings.cardBackgroundColor = color
                cardBackgroundColorPreview.setBackgroundColor(color)
                applyCardBackgroundColor()
            }
        }

        // Обработчики настроек спиннеров
        findViewById<Button>(R.id.btnSpinnerColor).setOnClickListener {
            showColorPicker("Выберите цвет спиннеров", appSettings.spinnerColor) { color ->
                appSettings.spinnerColor = color
                spinnerColorPreview.setBackgroundColor(color)
            }
        }

        spinnerCornerRadiusSlider.addOnChangeListener { _, value, _ ->
            val radius = value.toInt()
            appSettings.spinnerCornerRadius = radius
            spinnerCornerRadiusValue.text = "${radius}dp"
        }

        findViewById<Button>(R.id.btnResetSpinnerSettings).setOnClickListener {
            // Сброс к настройкам по умолчанию
            appSettings.backgroundColor = ContextCompat.getColor(this, android.R.color.white)
            appSettings.cardBackgroundColor = ContextCompat.getColor(this, android.R.color.white)
            appSettings.spinnerColor = ContextCompat.getColor(this, R.color.default_spinner_color)
            appSettings.spinnerCornerRadius = 8

            // Обновляем превью
            backgroundColorPreview.setBackgroundColor(appSettings.backgroundColor)
            cardBackgroundColorPreview.setBackgroundColor(appSettings.cardBackgroundColor)
            spinnerColorPreview.setBackgroundColor(appSettings.spinnerColor)
            spinnerCornerRadiusSlider.value = appSettings.spinnerCornerRadius.toFloat()
            spinnerCornerRadiusValue.text = "${appSettings.spinnerCornerRadius}dp"

            // Применяем изменения
            applyBackgroundColor()
            applyCardBackgroundColor()
        }

        // Обработчик изменения темы
        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                R.id.rbLightTheme -> AppSettings.ThemeMode.LIGHT
                R.id.rbDarkTheme -> AppSettings.ThemeMode.DARK
                R.id.rbSystemTheme -> AppSettings.ThemeMode.SYSTEM
                else -> AppSettings.ThemeMode.SYSTEM
            }
            appSettings.themeMode = themeMode
            applyTheme(themeMode)
        }
        
        // Настройка обработчиков для вкладки Топливо
        setupFuelListeners()
    }
    
    private fun initializeMonthChips() {
        monthNames.forEachIndexed { index, monthNameRes ->
            val chip = Chip(this)
            chip.text = getString(monthNameRes)
            chip.isCheckable = true
            chip.tag = index + 1 // Месяцы от 1 до 12
            chip.setOnCheckedChangeListener { _, isChecked ->
                saveSummerMonths()
            }
            summerMonthsChipGroup.addView(chip)
        }
    }
    
    private fun loadFuelSettings() {
        // Загружаем летние месяцы
        val summerMonths = appSettings.summerMonths
        for (i in 0 until summerMonthsChipGroup.childCount) {
            val chip = summerMonthsChipGroup.getChildAt(i) as Chip
            val monthNumber = chip.tag as Int
            chip.isChecked = summerMonths.contains(monthNumber)
        }
        
        // Загружаем расход топлива для Зубова
        val zubovSummer = appSettings.zubovSummerFuel
        if (zubovSummer > 0f) {
            etZubovSummerFuel.setText(zubovSummer.toString())
        } else {
            etZubovSummerFuel.setText("")
        }
        
        val zubovWinter = appSettings.zubovWinterFuel
        if (zubovWinter > 0f) {
            etZubovWinterFuel.setText(zubovWinter.toString())
        } else {
            etZubovWinterFuel.setText("")
        }
        
        // Загружаем расход топлива для СКД
        val skdSummer = appSettings.skdSummerFuel
        if (skdSummer > 0f) {
            etSkdSummerFuel.setText(skdSummer.toString())
        } else {
            etSkdSummerFuel.setText("")
        }
        
        val skdWinter = appSettings.skdWinterFuel
        if (skdWinter > 0f) {
            etSkdWinterFuel.setText(skdWinter.toString())
        } else {
            etSkdWinterFuel.setText("")
        }
    }
    
    private fun saveSummerMonths() {
        val selectedMonths = mutableSetOf<Int>()
        for (i in 0 until summerMonthsChipGroup.childCount) {
            val chip = summerMonthsChipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                val monthNumber = chip.tag as Int
                selectedMonths.add(monthNumber)
            }
        }
        appSettings.summerMonths = selectedMonths
    }
    
    private fun setupFuelListeners() {
        // Обработчики для полей ввода расхода топлива
        etZubovSummerFuel.addTextChangedListener(createFuelTextWatcher { value ->
            appSettings.zubovSummerFuel = value
        })
        
        etZubovWinterFuel.addTextChangedListener(createFuelTextWatcher { value ->
            appSettings.zubovWinterFuel = value
        })
        
        etSkdSummerFuel.addTextChangedListener(createFuelTextWatcher { value ->
            appSettings.skdSummerFuel = value
        })
        
        etSkdWinterFuel.addTextChangedListener(createFuelTextWatcher { value ->
            appSettings.skdWinterFuel = value
        })
    }
    
    private fun createFuelTextWatcher(onValueChanged: (Float) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                val value = text.toFloatOrNull() ?: 0f
                if (value >= 0f) {
                    onValueChanged(value)
                }
            }
        }
    }

    private fun showColorPicker(title: String, initialColor: Int, onColorSelected: (Int) -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_color_picker) // Используем НАШ полноценный макет

        // Устанавливаем заголовок
        val tvTitle = dialog.findViewById<TextView>(R.id.tvColorPickerTitle)
        tvTitle.text = title

        // Получаем элементы управления
        val colorPreview = dialog.findViewById<View>(R.id.colorPreview)
        val colorWheel = dialog.findViewById<View>(R.id.colorWheel) as? ColorWheelView

        // Если ColorWheelView не найден, используем только слайдеры
        if (colorWheel != null) {
            setupColorWheelPicker(dialog, colorWheel, colorPreview, initialColor, onColorSelected)
        } else {
            setupBasicColorPicker(dialog, colorPreview, initialColor, onColorSelected)
        }

        // Показываем диалог
        dialog.show()

        // Устанавливаем размер диалога
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )
    }

    private fun setupColorWheelPicker(
        dialog: Dialog,
        colorWheel: ColorWheelView,
        colorPreview: View,
        initialColor: Int,
        onColorSelected: (Int) -> Unit
    ) {
        val hueSlider = dialog.findViewById<Slider>(R.id.hueSlider)
        val saturationSlider = dialog.findViewById<Slider>(R.id.saturationSlider)
        val valueSlider = dialog.findViewById<Slider>(R.id.valueSlider)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

        // Устанавливаем начальный цвет
        var currentColor = initialColor
        colorPreview.setBackgroundColor(currentColor)

        // Преобразуем цвет в HSV для слайдеров
        val hsv = FloatArray(3)
        Color.colorToHSV(currentColor, hsv)

        // Устанавливаем значения слайдеров
        hueSlider.value = hsv[0] // Hue (0-360)
        saturationSlider.value = hsv[1] * 100 // Saturation (0-100)
        valueSlider.value = hsv[2] * 100 // Value/Brightness (0-100)

        // Настройка ColorWheel
        colorWheel.setOnColorChangedListener(object : ColorWheelView.OnColorChangedListener {
            override fun onColorChanged(color: Int) {
                currentColor = color
                colorPreview.setBackgroundColor(color)
                Color.colorToHSV(color, hsv)
                hueSlider.value = hsv[0]
                saturationSlider.value = hsv[1] * 100
                valueSlider.value = hsv[2] * 100
            }
        })
        colorWheel.setColor(currentColor)

        // Обработчики слайдеров
        hueSlider.addOnChangeListener { _, value, _ ->
            hsv[0] = value
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
            colorWheel.setColor(currentColor)
        }

        saturationSlider.addOnChangeListener { _, value, _ ->
            hsv[1] = value / 100f
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
            colorWheel.setColor(currentColor)
        }

        valueSlider.addOnChangeListener { _, value, _ ->
            hsv[2] = value / 100f
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
            colorWheel.setColor(currentColor)
        }

        // Кнопки
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener {
            onColorSelected(currentColor)
            dialog.dismiss()
        }
    }

    private fun setupBasicColorPicker(
        dialog: Dialog,
        colorPreview: View,
        initialColor: Int,
        onColorSelected: (Int) -> Unit
    ) {
        val hueSlider = dialog.findViewById<Slider>(R.id.hueSlider)
        val saturationSlider = dialog.findViewById<Slider>(R.id.saturationSlider)
        val valueSlider = dialog.findViewById<Slider>(R.id.valueSlider)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

        var currentColor = initialColor
        colorPreview.setBackgroundColor(currentColor)

        // Преобразуем цвет в HSV для слайдеров
        val hsv = FloatArray(3)
        Color.colorToHSV(currentColor, hsv)

        // Устанавливаем значения слайдеров
        hueSlider.value = hsv[0]
        saturationSlider.value = hsv[1] * 100
        valueSlider.value = hsv[2] * 100

        // Обработчики слайдеров
        hueSlider.addOnChangeListener { _, value, _ ->
            hsv[0] = value
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
        }

        saturationSlider.addOnChangeListener { _, value, _ ->
            hsv[1] = value / 100f
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
        }

        valueSlider.addOnChangeListener { _, value, _ ->
            hsv[2] = value / 100f
            currentColor = Color.HSVToColor(hsv)
            colorPreview.setBackgroundColor(currentColor)
        }

        // Кнопки
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener {
            onColorSelected(currentColor)
            dialog.dismiss()
        }
    }

    private fun updateCalendarColors() {
        // В реальном приложении можно использовать EventBus или LiveData
        // для обновления календаря в WorkActivity
        // Пока просто сохраняем настройки, календарь обновится при возврате в WorkActivity
    }


    private fun applyBackgroundColor() {
        // Изменяем цвет фона текущей активности
        val rootView = findViewById<View>(android.R.id.content)
        rootView.setBackgroundColor(appSettings.backgroundColor)
    }

    private fun applyCardBackgroundColor() {
        // Изменяем цвет фона карточек (если они есть в этой активности)
        // В SettingsActivity карточек может не быть, но функция нужна для совместимости
    }

    private fun applyTheme(themeMode: AppSettings.ThemeMode) {
        when (themeMode) {
            AppSettings.ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            AppSettings.ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            AppSettings.ThemeMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        // Перезапускаем активность для применения темы
        recreate()
    }
}