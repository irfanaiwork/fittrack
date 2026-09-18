package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY dayOfWeek ASC, time ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE dayOfWeek = :dayOfWeek ORDER BY time ASC")
    fun getExercisesForDay(dayOfWeek: Int): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises WHERE dayOfWeek = :dayOfWeek ORDER BY time ASC")
    suspend fun getExercisesForDayDirect(dayOfWeek: Int): List<Exercise>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Long): Exercise?

    @Query("SELECT * FROM exercises WHERE reminderEnabled = 1")
    suspend fun getExercisesForAlarms(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()
}
