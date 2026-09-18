package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // "30-minute workout", "Walk", "Walk / Rest"
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday, 7 = Sunday
    val time: String, // "21:30" (24-hour HH:mm)
    val duration: String = "30 min",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val isWorkout: Boolean = true // true = strength workout, false = walk / rest
)
