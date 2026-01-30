package com.example.zmeygorynych

import kotlinx.coroutines.flow.Flow
import java.util.Date

class WorkDayRepository(private val workDayDao: WorkDayDao) {

    suspend fun saveWorkDay(workDay: WorkDay) {
        workDayDao.insert(workDay)
    }

    suspend fun getWorkDayByDate(date: Long): WorkDay? {
        return workDayDao.getWorkDayByDate(date)
    }

    suspend fun getAllWorkDays(): List<WorkDay> {
        return workDayDao.getAllWorkDays()
    }

    fun getAllWorkDayDates(): Flow<List<Long>> {
        return workDayDao.getAllWorkDayDates()
    }

    fun getWorkDaysInRange(startDate: Date, endDate: Date): Flow<List<WorkDay>> {
        return workDayDao.getWorkDaysInRange(startDate.time, endDate.time)
    }

    suspend fun deleteWorkDay(workDay: WorkDay) {
        workDayDao.delete(workDay)
    }

    suspend fun deleteWorkDayByDate(date: Long) {
        workDayDao.deleteByDate(date)
    }
}