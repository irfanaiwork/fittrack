package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.CustomReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomReminderDao {
    @Query("SELECT * FROM custom_reminders ORDER BY time ASC")
    fun getAllCustomReminders(): Flow<List<CustomReminder>>

    @Query("SELECT * FROM custom_reminders WHERE id = :id LIMIT 1")
    suspend fun getCustomReminderById(id: Long): CustomReminder?

    @Query("SELECT * FROM custom_reminders WHERE enabled = 1")
    suspend fun getActiveReminders(): List<CustomReminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomReminder(reminder: CustomReminder): Long

    @Update
    suspend fun updateCustomReminder(reminder: CustomReminder)

    @Delete
    suspend fun deleteCustomReminder(reminder: CustomReminder)
}
