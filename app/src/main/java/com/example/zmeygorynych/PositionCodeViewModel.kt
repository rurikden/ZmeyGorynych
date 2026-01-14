package com.example.zmeygorynych

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PositionCodeViewModel(private val repository: PositionCodeRepository) : ViewModel() {

    private val _positionCodes = MutableStateFlow<List<PositionCode>>(emptyList())
    val positionCodes: StateFlow<List<PositionCode>> = _positionCodes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPositionCodes()
    }

    private fun loadPositionCodes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val codes = repository.getAllPositionCodes()
                _positionCodes.value = codes
                // Обновляем кэш в Personnel классе
                Personnel.setPositionCodesCache(codes)
            } catch (e: Exception) {
                // Обработка ошибок
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPositionCode(shortCode: String, fullTitle: String, category: String) {
        viewModelScope.launch {
            try {
                repository.addPositionCode(
                    PositionCode(
                        shortCode = shortCode.trim(),
                        fullTitle = fullTitle.trim(),
                        category = category.trim()
                    )
                )
                loadPositionCodes() // Перезагрузка списка
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    fun updatePositionCode(positionCode: PositionCode, newShortCode: String, newFullTitle: String, newCategory: String) {
        viewModelScope.launch {
            try {
                val updatedCode = positionCode.copy(
                    shortCode = newShortCode.trim(),
                    fullTitle = newFullTitle.trim(),
                    category = newCategory.trim()
                )
                repository.updatePositionCode(updatedCode)
                loadPositionCodes() // Перезагрузка списка
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    fun deletePositionCode(positionCode: PositionCode) {
        viewModelScope.launch {
            try {
                repository.deletePositionCode(positionCode)
                loadPositionCodes() // Перезагрузка списка
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

    fun getPositionCodeByShortCode(shortCode: String): PositionCode? {
        return _positionCodes.value.find { it.shortCode == shortCode }
    }

    // Метод для инициализации дефолтных значений (вызывается из Activity)
    fun initializeDefaults() {
        viewModelScope.launch {
            try {
                repository.initializeDefaultPositionCodes()
                loadPositionCodes()
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
}
