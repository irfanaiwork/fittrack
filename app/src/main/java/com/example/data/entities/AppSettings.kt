package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val notificationEnabled: Boolean = true,
    val defaultSnoozeMinutes: Int = 5,
    val alarmSound: String = "Default",
    val onboardingCompleted: Boolean = false,
    val waterTargetGlasses: Int = 8,
    val waterConsumedGlasses: Int = 0,
    val waterDate: String = "",
    val selectedPlan: String = "LOW" // "LOW", "MEDIUM", "HIGH", "CUSTOM"
)
