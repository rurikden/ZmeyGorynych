package com.example.zmeygorynych

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personnel")
data class Personnel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val position: String,
    val company: String,
    val fullPosition: String = "" // Полное название должности
) {
    val fullName: String
        get() = "$lastName $firstName $middleName"

    // Вычисляемое свойство для отображения полной должности
    val displayPosition: String
        get() = if (fullPosition.isNotEmpty()) fullPosition else expandPosition(position)

    companion object {
        fun expandPosition(shortPosition: String): String {
            val trimmed = shortPosition.trim()

            return when {
                // ПМС (Путевая машинная станция)
                trimmed.matches(Regex("ПМС \\d+")) -> {
                    "Начальник $trimmed"
                }
                trimmed.matches(Regex("ПМСЗ \\d+")) -> {
                    val number = trimmed.substringAfter("ПМСЗ ")
                    "Заместитель начальника ПМС $number"
                }
                trimmed.matches(Regex("ПМСГ \\d+")) -> {
                    val number = trimmed.substringAfter("ПМСГ ")
                    "Главный инженер ПМС $number"
                }
                trimmed.matches(Regex("ПД ПМС \\d+")) -> {
                    val number = trimmed.substringAfter("ПД ПМС ")
                    "Дорожный мастер ПМС $number"
                }

                // ПЧ (Путевая часть)
                trimmed.matches(Regex("ПЧ \\d+")) -> {
                    "Начальник $trimmed"
                }
                trimmed.matches(Regex("ПЧЗ \\d+")) -> {
                    val number = trimmed.substringAfter("ПЧЗ ")
                    "Заместитель начальника ПЧ $number"
                }
                trimmed.matches(Regex("ПЧГ \\d+")) -> {
                    val number = trimmed.substringAfter("ПЧГ ")
                    "Главный инженер ПЧ $number"
                }

                // Если не распознано - возвращаем как есть
                else -> trimmed
            }
        }
    }
}

