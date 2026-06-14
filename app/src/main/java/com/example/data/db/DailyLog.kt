package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val isLogged: Boolean = true,
    val intensity: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
