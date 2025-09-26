package com.example.echotasks

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val timeInMillis: Long,
    val color: String = "white",
    val isCompleted: Boolean = false
)
