package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val notificationEnabled: Boolean = true,
    val defaultSnoozeMinutes: Int = 5,
    val alarmSound: String = "Continuous Phone Ring",
    val onboardingCompleted: Boolean = false,
    val isTrackingStarted: Boolean = false,
    val trackingStartDate: String = "",
    val waterTargetGlasses: Int = 8,
    val waterConsumedGlasses: Int = 0,
    val waterDate: String = "",
    val selectedPlan: String = "LOW", // "LOW", "MEDIUM", "HIGH", "CUSTOM"
    // User Profile for Calorie and Burn Calculation
    val userWeightKg: Float = 0f, // 0 = not configured yet
    val userHeightCm: Float = 0f, // 0 = not configured yet
    val userAge: Int = 0, // 0 = not configured yet
    val userGender: String = "", // "Male", "Female"
    val userActivityLevel: String = "Moderate", // "Sedentary", "Light", "Moderate", "Active"
    val userGoal: String = "Maintain Weight", // "Lose Fat / Deficit", "Maintain Weight", "Build Muscle / Bulk"
    val dailyCalorieTarget: Int = 2000,
    val alarmContinuousRinging: Boolean = true, // ring continuously like an incoming call / alarm
    // GitHub App Updates
    val githubOwner: String = "irfan-aiwork",
    val githubRepo: String = "fit-track",
    val autoCheckUpdates: Boolean = true,
    val lastDismissedUpdateVersion: String = ""
)
