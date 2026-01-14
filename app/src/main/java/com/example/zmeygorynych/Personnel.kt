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
        get() = if (fullPosition.isNotEmpty()) fullPosition else expandPosition(position, company)

    companion object {
        fun expandPosition(shortPosition: String, company: String): String {
            val positionTrimmed = shortPosition.trim()
            val companyTrimmed = company.trim()

            // Извлекаем номер из поля company (например, "ПМСГ 110" -> "110")
            val number = extractNumberFromCompany(companyTrimmed)

            return when {
                // ПМС (Путевая машинная станция)
                positionTrimmed == "ПМС" && number.isNotEmpty() -> {
                    "Начальник ПМС $number"
                }
                positionTrimmed == "ПМСЗ" && number.isNotEmpty() -> {
                    "Заместитель начальника ПМС $number"
                }
                positionTrimmed == "ПМСГ" && number.isNotEmpty() -> {
                    "Главный инженер ПМС $number"
                }
                positionTrimmed == "ПД" && companyTrimmed.contains("ПМС") && number.isNotEmpty() -> {
                    "Дорожный мастер ПМС $number"
                }

                // ПЧ (Путевая часть)
                positionTrimmed == "ПЧ" && number.isNotEmpty() -> {
                    "Начальник ПЧ $number"
                }
                positionTrimmed == "ПЧЗ" && number.isNotEmpty() -> {
                    "Заместитель начальника ПЧ $number"
                }
                positionTrimmed == "ПЧГ" && number.isNotEmpty() -> {
                    "Главный инженер ПЧ $number"
                }

                // Если не распознано - возвращаем сокращение + номер
                number.isNotEmpty() -> "$positionTrimmed $number"
                else -> positionTrimmed
            }
        }

        private fun extractNumberFromCompany(company: String): String {
            // Ищем паттерны типа "ПМСГ 110", "ПЧ 33", "ПМС 45" и т.д.
            val patterns = listOf(
                Regex("ПМСГ (\\d+)"),
                Regex("ПМСЗ (\\d+)"),
                Regex("ПМС (\\d+)"),
                Regex("ПЧГ (\\d+)"),
                Regex("ПЧЗ (\\d+)"),
                Regex("ПЧ (\\d+)"),
                Regex("ПД ПМС (\\d+)")
            )

            for (pattern in patterns) {
                val match = pattern.find(company)
                if (match != null) {
                    return match.groupValues[1]
                }
            }

            // Если не нашли паттерн, пробуем найти просто число в конце строки
            val numberMatch = Regex("(\\d+)$").find(company)
            return numberMatch?.groupValues?.get(1) ?: ""
        }
    }
}

