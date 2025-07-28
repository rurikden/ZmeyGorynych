package com.example.zmeygorynych

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContextCompat

class AppSettings private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "app_settings"
        private const val KEY_SELECTED_DAY_COLOR = "selected_day_color"
        private const val KEY_CURRENT_DAY_COLOR = "current_day_color"
        private const val KEY_WEEKEND_COLOR = "weekend_color"
        private const val KEY_THEME_MODE = "theme_mode"

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

    var themeMode: ThemeMode
        get() = ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.name).apply()

    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
}