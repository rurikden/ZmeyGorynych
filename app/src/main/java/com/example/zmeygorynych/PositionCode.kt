package com.example.zmeygorynych

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "position_codes")
data class PositionCode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shortCode: String,  // Сокращение (ПМСГ, ПМСЗ, ПЧ, etc.)
    val fullTitle: String,  // Полное название (Главный инженер, Заместитель начальника, etc.)
    val category: String    // Категория (ПМС, ПЧ, etc.) для группировки
) {
    // Для отображения в списке
    val displayName: String
        get() = "$shortCode → $fullTitle (${category})"
}
