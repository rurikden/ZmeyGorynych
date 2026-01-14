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
        // Кэш для кодов должностей (загружается из базы данных)
        private var positionCodesCache: List<PositionCode> = emptyList()

        // Метод для установки кэша кодов должностей
        fun setPositionCodesCache(codes: List<PositionCode>) {
            positionCodesCache = codes
        }

        fun expandPosition(shortPosition: String, company: String): String {
            val positionTrimmed = shortPosition.trim()

            // Сначала пробуем найти в кэше кодов должностей
            val cachedCode = positionCodesCache.find { it.shortCode == positionTrimmed }
            if (cachedCode != null) {
                val number = extractNumberFromCompany(company)
                return if (number.isNotEmpty()) {
                    "${cachedCode.fullTitle} ${cachedCode.category} $number"
                } else {
                    "${cachedCode.fullTitle} (${cachedCode.category})"
                }
            }

            // Fallback: старый алгоритм
            return expandPositionLegacy(shortPosition, company)
        }

        private fun expandPositionLegacy(shortPosition: String, company: String): String {
            val positionTrimmed = shortPosition.trim()
            val companyTrimmed = company.trim()

            // Извлекаем номер из поля company
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
            // Более гибкий подход: ищем число после ключевых слов
            val companyUpper = company.uppercase()

            // Ищем паттерны с числами после ПМС или ПЧ
            val pmcPattern = Regex("(?:ПМС|ПЧ).*?(\\d+)", RegexOption.IGNORE_CASE)
            val match = pmcPattern.find(companyUpper)

            return if (match != null) {
                match.groupValues[1]
            } else {
                // Fallback: любое число в конце строки
                val numberMatch = Regex("(\\d+)$").find(company)
                numberMatch?.groupValues?.get(1) ?: ""
            }
        }
    }
}

