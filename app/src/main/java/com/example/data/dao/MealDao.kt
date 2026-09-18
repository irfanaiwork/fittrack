package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.Meal
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY time ASC")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE enabled = 1 ORDER BY time ASC")
    fun getEnabledMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: Long): Meal?

    @Query("SELECT * FROM meals WHERE enabled = 1 AND reminderEnabled = 1")
    suspend fun getMealsForAlarms(): List<Meal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<Meal>)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()
}
