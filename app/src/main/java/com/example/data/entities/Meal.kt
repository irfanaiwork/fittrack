package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Breakfast, Lunch, Evening Snack, Dinner
    val food: String, // Eggs + Bread + Chai
    val quantity: String, // 2 eggs + 2 bread slices + 1 cup chai, kam cheeni
    val time: String, // "08:00" in 24-hour HH:mm
    val displayTimeRange: String = "", // "8:00–9:00 AM"
    val enabled: Boolean = true,
    val reminderEnabled: Boolean = true,
    val orderIndex: Int = 0,
    val calories: Int = 350
)
