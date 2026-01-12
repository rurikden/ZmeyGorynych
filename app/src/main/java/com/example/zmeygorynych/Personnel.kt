package com.example.zmeygorynych

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personnel")
data class Personnel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lastName: String,
    val firstName: String,
    val middleName: String,
    val position: String,
    val company: String
) {
    val fullName: String
        get() = "$lastName $firstName $middleName"
}

