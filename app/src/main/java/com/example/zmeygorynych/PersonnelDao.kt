package com.example.zmeygorynych

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonnelDao {

    @Query("SELECT * FROM personnel ORDER BY lastName, firstName, middleName")
    fun getAllPersonnel(): Flow<List<Personnel>>

    @Query("SELECT * FROM personnel WHERE lastName LIKE '%' || :query || '%' OR firstName LIKE '%' || :query || '%' OR middleName LIKE '%' || :query || '%' OR position LIKE '%' || :query || '%' ORDER BY lastName, firstName, middleName")
    fun searchPersonnel(query: String): Flow<List<Personnel>>

    @Insert
    suspend fun insertPersonnel(personnel: Personnel): Long

    @Update
    suspend fun updatePersonnel(personnel: Personnel)

    @Delete
    suspend fun deletePersonnel(personnel: Personnel)

    @Query("DELETE FROM personnel")
    suspend fun deleteAllPersonnel()

    @Query("SELECT * FROM personnel WHERE id = :id")
    suspend fun getPersonnelById(id: Long): Personnel?

    @Query("SELECT * FROM personnel WHERE position LIKE '%машинист%' OR position LIKE '%Машинист%' ORDER BY lastName, firstName, middleName")
    suspend fun getMachinists(): List<Personnel>

    @Query("SELECT * FROM personnel WHERE position NOT LIKE '%машинист%' AND position NOT LIKE '%Машинист%' ORDER BY lastName, firstName, middleName")
    suspend fun getNonManagers(): List<Personnel>

    @Query("SELECT * FROM personnel WHERE (position LIKE '%машинист%' OR position LIKE '%Машинист%') AND (lastName LIKE '%' || :query || '%' OR firstName LIKE '%' || :query || '%' OR middleName LIKE '%' || :query || '%') ORDER BY lastName, firstName, middleName")
    suspend fun searchMachinists(query: String): List<Personnel>

    @Query("SELECT * FROM personnel WHERE position NOT LIKE '%машинист%' AND position NOT LIKE '%Машинист%' AND (lastName LIKE '%' || :query || '%' OR firstName LIKE '%' || :query || '%' OR middleName LIKE '%' || :query || '%') ORDER BY lastName, firstName, middleName")
    suspend fun searchNonManagers(query: String): List<Personnel>
}

