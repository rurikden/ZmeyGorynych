package com.example.zmeygorynych

import kotlinx.coroutines.flow.Flow

class WorkTypeRepository(private val workTypeDao: WorkTypeDao) {

    suspend fun insert(workType: WorkType) {
        workTypeDao.insert(workType)
    }

    fun getWorkTypesByType(type: String): Flow<List<WorkType>> {
        return workTypeDao.getWorkTypesByType(type)
    }

    suspend fun deleteById(id: Long) {
        workTypeDao.deleteById(id)
    }
}
