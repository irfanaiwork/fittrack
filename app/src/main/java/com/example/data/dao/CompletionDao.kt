package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entities.ActivityCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
    @Query("SELECT * FROM activity_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<ActivityCompletion>>

    @Query("SELECT * FROM activity_completions WHERE date = :date")
    suspend fun getCompletionsForDateDirect(date: String): List<ActivityCompletion>

    @Query("SELECT * FROM activity_completions WHERE date BETWEEN :startDate AND :endDate")
    fun getCompletionsForDateRange(startDate: String, endDate: String): Flow<List<ActivityCompletion>>

    @Query("SELECT * FROM activity_completions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getCompletionsForDateRangeDirect(startDate: String, endDate: String): List<ActivityCompletion>

    @Query("SELECT * FROM activity_completions ORDER BY date DESC, scheduledTime DESC")
    fun getAllCompletions(): Flow<List<ActivityCompletion>>

    @Query("SELECT * FROM activity_completions WHERE activityId = :activityId AND activityType = :activityType AND date = :date LIMIT 1")
    suspend fun getCompletion(activityId: Long, activityType: String, date: String): ActivityCompletion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCompletion(completion: ActivityCompletion): Long

    @Query("DELETE FROM activity_completions WHERE activityId = :activityId AND activityType = :activityType AND date = :date")
    suspend fun deleteCompletion(activityId: Long, activityType: String, date: String)

    @Query("DELETE FROM activity_completions")
    suspend fun clearAllCompletions()
}
