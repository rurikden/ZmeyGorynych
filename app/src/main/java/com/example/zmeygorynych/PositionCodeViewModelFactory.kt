package com.example.zmeygorynych

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PositionCodeViewModelFactory(private val repository: PositionCodeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PositionCodeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PositionCodeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
