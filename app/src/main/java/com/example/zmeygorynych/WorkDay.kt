package com.example.zmeygorynych

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "work_days")
data class WorkDay(
    @PrimaryKey
    val date: Long,

    // Виды работ
    val zubovWorkType: String? = null,
    val skdWorkType: String? = null,

    // Персонал
    val machinist1: String? = null,
    val machinist2: String? = null,
    val manager: String? = null,

    // Перегоны
    val peregon1: String? = null,
    val peregon2: String? = null,

    // Время окон
    val windowFrom1: String? = null,
    val windowTo1: String? = null,
    val windowFrom2: String? = null,
    val windowTo2: String? = null,

    // Время СКД
    val skdFrom1: String? = null,
    val skdTo1: String? = null,
    val skdFrom2: String? = null,
    val skdTo2: String? = null,

    // Время зубов
    val zubovFrom1: String? = null,
    val zubovTo1: String? = null,
    val zubovFrom2: String? = null,
    val zubovTo2: String? = null,

    // Часы (можно вычислять, но сохраним для удобства)
    val windowHours1: Int? = null,
    val windowHours2: Int? = null,
    val skdHours1: Int? = null,
    val skdHours2: Int? = null,
    val zubovHours1: Int? = null,
    val zubovHours2: Int? = null
)