package com.example.zmeygorynych

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PositionCodeRepository(private val positionCodeDao: PositionCodeDao) {

    suspend fun addPositionCode(positionCode: PositionCode): Long =
        positionCodeDao.insertPositionCode(positionCode)

    suspend fun updatePositionCode(positionCode: PositionCode) =
        positionCodeDao.updatePositionCode(positionCode)

    suspend fun deletePositionCode(positionCode: PositionCode) =
        positionCodeDao.deletePositionCode(positionCode)

    suspend fun getAllPositionCodes(): List<PositionCode> =
        positionCodeDao.getAllPositionCodes()

    suspend fun getPositionCodeByShortCode(shortCode: String): PositionCode? =
        positionCodeDao.getPositionCodeByShortCode(shortCode)

    suspend fun getPositionCodesByCategory(category: String): List<PositionCode> =
        positionCodeDao.getPositionCodesByCategory(category)

    suspend fun deleteAllPositionCodes() =
        positionCodeDao.deleteAllPositionCodes()

    // Метод для инициализации дефолтных значений
    suspend fun initializeDefaultPositionCodes() {
        val existingCodes = getAllPositionCodes()
        if (existingCodes.isEmpty()) {
            val defaultCodes = listOf(
                // ПМС (Путевая машинная станция)
                PositionCode(shortCode = "ПМС", fullTitle = "Начальник", category = "ПМС"),
                PositionCode(shortCode = "ПМСЗ", fullTitle = "Заместитель начальника", category = "ПМС"),
                PositionCode(shortCode = "ПМСГ", fullTitle = "Главный инженер", category = "ПМС"),

                // ПЧ (Путевая часть)
                PositionCode(shortCode = "ПЧ", fullTitle = "Начальник", category = "ПЧ"),
                PositionCode(shortCode = "ПЧЗ", fullTitle = "Заместитель начальника", category = "ПЧ"),
                PositionCode(shortCode = "ПЧГ", fullTitle = "Главный инженер", category = "ПЧ"),

                // ПД (Дорожный мастер)
                PositionCode(shortCode = "ПД", fullTitle = "Дорожный мастер", category = "ПМС")
            )

            defaultCodes.forEach { addPositionCode(it) }
        }
    }
}
