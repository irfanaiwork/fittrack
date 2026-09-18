package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.WorkoutRoutineItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_items ORDER BY orderIndex ASC, id ASC")
    fun getAllWorkoutItems(): Flow<List<WorkoutRoutineItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WorkoutRoutineItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<WorkoutRoutineItem>)

    @Update
    suspend fun updateItem(item: WorkoutRoutineItem)

    @Delete
    suspend fun deleteItem(item: WorkoutRoutineItem)

    @Query("DELETE FROM workout_items")
    suspend fun deleteAllItems()
}
