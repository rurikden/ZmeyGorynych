package com.example.zmeygorynych

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import org.json.JSONArray

class AppSettings private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_SELECTED_DAY_COLOR = "selected_day_color"
        private const val KEY_CURRENT_DAY_COLOR = "current_day_color"
        private const val KEY_WEEKEND_COLOR = "weekend_color"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SPINNER_COLOR = "spinner_color"
        private const val KEY_SPINNER_CORNER_RADIUS = "spinner_corner_radius"
        private const val KEY_BACKGROUND_COLOR = "background_color"
        private const val KEY_CARD_BACKGROUND_COLOR = "card_background_color"
        
        // Ключи для настроек топлива
        private const val KEY_SUMMER_MONTHS = "summer_months"
        private const val KEY_ZUBOV_SUMMER_FUEL = "zubov_summer_fuel"
        private const val KEY_ZUBOV_WINTER_FUEL = "zubov_winter_fuel"
        private const val KEY_SKD_SUMMER_FUEL = "skd_summer_fuel"
        private const val KEY_SKD_WINTER_FUEL = "skd_winter_fuel"

        @Volatile
        private var INSTANCE: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSettings(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Цвета по умолчанию
    private val defaultSelectedDayColor = ContextCompat.getColor(context, R.color.purple_500)
    private val defaultCurrentDayColor = ContextCompat.getColor(context, R.color.teal_200)
    private val defaultWeekendColor = ContextCompat.getColor(context, R.color.red_500)
    private val defaultSpinnerColor = ContextCompat.getColor(context, android.R.color.white)
    private val defaultBackgroundColor = ContextCompat.getColor(context, android.R.color.white)
    private val defaultCardBackgroundColor = ContextCompat.getColor(context, android.R.color.white)

    // Геттеры и сеттеры для цветов
    var selectedDayColor: Int
        get() = prefs.getInt(KEY_SELECTED_DAY_COLOR, defaultSelectedDayColor)
        set(value) = prefs.edit().putInt(KEY_SELECTED_DAY_COLOR, value).apply()

    var currentDayColor: Int
        get() = prefs.getInt(KEY_CURRENT_DAY_COLOR, defaultCurrentDayColor)
        set(value) = prefs.edit().putInt(KEY_CURRENT_DAY_COLOR, value).apply()

    var weekendColor: Int
        get() = prefs.getInt(KEY_WEEKEND_COLOR, defaultWeekendColor)
        set(value) = prefs.edit().putInt(KEY_WEEKEND_COLOR, value).apply()

    var spinnerColor: Int
        get() = prefs.getInt(KEY_SPINNER_COLOR, defaultSpinnerColor)
        set(value) = prefs.edit().putInt(KEY_SPINNER_COLOR, value).apply()

    var spinnerCornerRadius: Int
        get() = prefs.getInt(KEY_SPINNER_CORNER_RADIUS, 8)
        set(value) = prefs.edit().putInt(KEY_SPINNER_CORNER_RADIUS, value).apply()

    var backgroundColor: Int
        get() = prefs.getInt(KEY_BACKGROUND_COLOR, defaultBackgroundColor)
        set(value) = prefs.edit().putInt(KEY_BACKGROUND_COLOR, value).apply()

    var cardBackgroundColor: Int
        get() = prefs.getInt(KEY_CARD_BACKGROUND_COLOR, defaultCardBackgroundColor)
        set(value) = prefs.edit().putInt(KEY_CARD_BACKGROUND_COLOR, value).apply()

    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()

    // Настройки топлива
    var summerMonths: Set<Int>
        get() {
            val jsonString = prefs.getString(KEY_SUMMER_MONTHS, "[]") ?: "[]"
            return try {
                val jsonArray = JSONArray(jsonString)
                (0 until jsonArray.length()).map { jsonArray.getInt(it) }.toSet()
            } catch (e: Exception) {
                emptySet()
            }
        }
        set(value) {
            val jsonArray = JSONArray()
            value.sorted().forEach { jsonArray.put(it) }
            prefs.edit().putString(KEY_SUMMER_MONTHS, jsonArray.toString()).apply()
        }

    var zubovSummerFuel: Float
        get() = prefs.getFloat(KEY_ZUBOV_SUMMER_FUEL, 0f)
        set(value) = prefs.edit().putFloat(KEY_ZUBOV_SUMMER_FUEL, value).apply()

    var zubovWinterFuel: Float
        get() = prefs.getFloat(KEY_ZUBOV_WINTER_FUEL, 0f)
        set(value) = prefs.edit().putFloat(KEY_ZUBOV_WINTER_FUEL, value).apply()

    var skdSummerFuel: Float
        get() = prefs.getFloat(KEY_SKD_SUMMER_FUEL, 0f)
        set(value) = prefs.edit().putFloat(KEY_SKD_SUMMER_FUEL, value).apply()

    var skdWinterFuel: Float
        get() = prefs.getFloat(KEY_SKD_WINTER_FUEL, 0f)
        set(value) = prefs.edit().putFloat(KEY_SKD_WINTER_FUEL, value).apply()

    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
}