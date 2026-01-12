package com.example.zmeygorynych

import kotlinx.coroutines.flow.Flow

class PersonnelRepository(private val personnelDao: PersonnelDao) {

    fun getAllPersonnel(): Flow<List<Personnel>> = personnelDao.getAllPersonnel()

    fun searchPersonnel(query: String): Flow<List<Personnel>> = personnelDao.searchPersonnel(query)

    suspend fun addPersonnel(personnel: Personnel): Long = personnelDao.insertPersonnel(personnel)

    suspend fun deletePersonnel(personnel: Personnel) = personnelDao.deletePersonnel(personnel)

    suspend fun getMachinists(): List<Personnel> = personnelDao.getMachinists()

    suspend fun getNonManagers(): List<Personnel> = personnelDao.getNonManagers()

    suspend fun searchMachinists(query: String): List<Personnel> = personnelDao.searchMachinists(query)

    suspend fun searchNonManagers(query: String): List<Personnel> = personnelDao.searchNonManagers(query)
}


