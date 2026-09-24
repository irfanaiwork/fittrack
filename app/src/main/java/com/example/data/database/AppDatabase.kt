package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CompletionDao
import com.example.data.dao.CustomReminderDao
import com.example.data.dao.ExerciseDao
import com.example.data.dao.MealDao
import com.example.data.dao.SettingsDao
import com.example.data.dao.WorkoutDao
import com.example.data.entities.ActivityCompletion
import com.example.data.entities.AppSettings
import com.example.data.entities.CustomReminder
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import com.example.data.entities.WorkoutRoutineItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Meal::class,
        Exercise::class,
        WorkoutRoutineItem::class,
        ActivityCompletion::class,
        CustomReminder::class,
        AppSettings::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun completionDao(): CompletionDao
    abstract fun customReminderDao(): CustomReminderDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fittrack_daily.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate database with default diet, exercise, workout and settings
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getInstance(context)
                                database.mealDao().insertMeals(DefaultData.getDefaultMeals())
                                database.exerciseDao().insertExercises(DefaultData.getDefaultExercises())
                                database.workoutDao().insertItems(DefaultData.getDefaultWorkoutItems())
                                database.settingsDao().insertOrUpdate(DefaultData.getDefaultSettings())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
