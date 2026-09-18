package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_items")
data class WorkoutRoutineItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sets: String,
    val reps: String,
    val duration: String,
    val notes: String = "",
    val orderIndex: Int = 0
)
