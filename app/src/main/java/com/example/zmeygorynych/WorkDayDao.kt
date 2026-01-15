package com.example.zmeygorynych

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface WorkDayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workDay: WorkDay)

    @Query("SELECT * FROM work_days WHERE date = :date LIMIT 1")
    suspend fun getWorkDayByDate(date: Long): WorkDay?

    @Query("SELECT date FROM work_days ORDER BY date")
    fun getAllWorkDayDates(): Flow<List<Long>>

    @Query("SELECT * FROM work_days WHERE date >= :startDate AND date <= :endDate ORDER BY date")
    fun getWorkDaysInRange(startDate: Long, endDate: Long): Flow<List<WorkDay>>

    @Delete
    suspend fun delete(workDay: WorkDay)

    @Query("DELETE FROM work_days WHERE date = :date")
    suspend fun deleteByDate(date: Long)
}