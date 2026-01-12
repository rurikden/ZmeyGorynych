package com.example.zmeygorynych

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkTypeDao {
    @Insert
    suspend fun insert(workType: WorkType)

    @Query("SELECT * FROM work_types WHERE type = :type ORDER BY name ASC")
    fun getWorkTypesByType(type: String): Flow<List<WorkType>>

    @Query("DELETE FROM work_types WHERE id = :id")
    suspend fun deleteById(id: Long)
}
