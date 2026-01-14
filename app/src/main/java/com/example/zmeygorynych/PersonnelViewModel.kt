package com.example.zmeygorynych

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PersonnelViewModel(private val repository: PersonnelRepository) : ViewModel() {

    private val _personnelList = MutableStateFlow<List<Personnel>>(emptyList())
    val personnelList: StateFlow<List<Personnel>> = _personnelList.asStateFlow()

    private var currentQuery: String = ""

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            repository.getAllPersonnel().collectLatest { list ->
                if (currentQuery.isBlank()) {
                    _personnelList.value = list
                }
            }
        }
    }

    fun searchPersonnel(query: String) {
        currentQuery = query
        viewModelScope.launch {
            if (query.isBlank()) {
                repository.getAllPersonnel().collectLatest { list ->
                    _personnelList.value = list
                }
            } else {
                repository.searchPersonnel(query).collectLatest { list ->
                    _personnelList.value = list
                }
            }
        }
    }

    fun addPersonnel(
        lastName: String,
        firstName: String,
        middleName: String,
        position: String,
        company: String
    ) {
        viewModelScope.launch {
            repository.addPersonnel(
                Personnel(
                    lastName = lastName,
                    firstName = firstName,
                    middleName = middleName,
                    position = position,
                    company = company,
                    fullPosition = Personnel.expandPosition(position)
                )
            )
            if (currentQuery.isNotBlank()) {
                searchPersonnel(currentQuery)
            }
        }
    }

    fun deletePersonnel(personnel: Personnel) {
        viewModelScope.launch {
            repository.deletePersonnel(personnel)
            if (currentQuery.isNotBlank()) {
                searchPersonnel(currentQuery)
            }
        }
    }

    fun updatePersonnel(
        personnelId: Long,
        lastName: String,
        firstName: String,
        middleName: String,
        position: String,
        company: String
    ) {
        viewModelScope.launch {
            val updatedPersonnel = Personnel(
                id = personnelId,
                lastName = lastName,
                firstName = firstName,
                middleName = middleName,
                position = position,
                company = company,
                fullPosition = Personnel.expandPosition(position)
            )
            repository.updatePersonnel(updatedPersonnel)
        }
    }

    fun deletePersonnel(personnelId: Long) {
        viewModelScope.launch {
            // Найти сотрудника по ID и удалить
            val personnelToDelete = personnelList.value.find { it.id == personnelId }
            personnelToDelete?.let { repository.deletePersonnel(it) }
        }
    }
}

class PersonnelViewModelFactory(private val repository: PersonnelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonnelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonnelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


