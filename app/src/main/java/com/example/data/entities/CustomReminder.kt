package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_reminders")
data class CustomReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val time: String, // "18:30" (24-hour HH:mm)
    val repeatType: String = "DAILY", // "ONCE", "DAILY", "WEEKLY", "SELECTED_DAYS"
    val selectedDays: String = "1,2,3,4,5,6,7", // 1=Mon .. 7=Sun
    val enabled: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 5
)
