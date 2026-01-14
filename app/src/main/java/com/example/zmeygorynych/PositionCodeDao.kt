package com.example.zmeygorynych

import androidx.room.*

@Dao
interface PositionCodeDao {
    @Insert
    suspend fun insertPositionCode(positionCode: PositionCode): Long

    @Update
    suspend fun updatePositionCode(positionCode: PositionCode)

    @Delete
    suspend fun deletePositionCode(positionCode: PositionCode)

    @Query("SELECT * FROM position_codes ORDER BY id ASC")
    suspend fun getAllPositionCodes(): List<PositionCode>

    @Query("SELECT * FROM position_codes WHERE shortCode = :shortCode LIMIT 1")
    suspend fun getPositionCodeByShortCode(shortCode: String): PositionCode?

    @Query("SELECT * FROM position_codes WHERE category = :category ORDER BY id ASC")
    suspend fun getPositionCodesByCategory(category: String): List<PositionCode>

    @Query("DELETE FROM position_codes")
    suspend fun deleteAllPositionCodes()
}
