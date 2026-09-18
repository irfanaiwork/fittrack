package com.example.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_completions",
    indices = [Index(value = ["activityId", "activityType", "date"], unique = true)]
)
data class ActivityCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: Long,
    val activityType: String, // "MEAL", "EXERCISE", "CUSTOM"
    val date: String, // "yyyy-MM-dd"
    val scheduledTime: String,
    val completed: Boolean, // true = Yes Done, false = No / Missed
    val completionTime: String? = null, // e.g. "08:15 AM"
    val note: String? = null // Reason if not completed: "Not hungry", "Busy", "Missed it", "Too tired", "Other"
)
