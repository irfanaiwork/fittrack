package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.DefaultData
import com.example.data.entities.ActivityCompletion
import com.example.data.entities.AppSettings
import com.example.data.entities.CustomReminder
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import com.example.data.entities.WorkoutRoutineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FitTrackRepository(private val database: AppDatabase) {
    private val mealDao = database.mealDao()
    private val exerciseDao = database.exerciseDao()
    private val workoutDao = database.workoutDao()
    private val completionDao = database.completionDao()
    private val customReminderDao = database.customReminderDao()
    private val settingsDao = database.settingsDao()

    // Meals
    val allMeals: Flow<List<Meal>> = mealDao.getAllMeals()
    val enabledMeals: Flow<List<Meal>> = mealDao.getEnabledMeals()

    suspend fun getMealById(id: Long): Meal? = mealDao.getMealById(id)
    suspend fun insertMeal(meal: Meal): Long = mealDao.insertMeal(meal)
    suspend fun updateMeal(meal: Meal) = mealDao.updateMeal(meal)
    suspend fun deleteMeal(meal: Meal) = mealDao.deleteMeal(meal)
    suspend fun getMealsForAlarms(): List<Meal> = mealDao.getMealsForAlarms()

    // Exercises
    val allExercises: Flow<List<Exercise>> = exerciseDao.getAllExercises()
    fun getExercisesForDay(dayOfWeek: Int): Flow<List<Exercise>> = exerciseDao.getExercisesForDay(dayOfWeek)
    suspend fun getExercisesForDayDirect(dayOfWeek: Int): List<Exercise> = exerciseDao.getExercisesForDayDirect(dayOfWeek)
    suspend fun insertExercise(exercise: Exercise): Long = exerciseDao.insertExercise(exercise)
    suspend fun updateExercise(exercise: Exercise) = exerciseDao.updateExercise(exercise)
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.deleteExercise(exercise)
    suspend fun getExercisesForAlarms(): List<Exercise> = exerciseDao.getExercisesForAlarms()

    // Workout Routine
    val workoutItems: Flow<List<WorkoutRoutineItem>> = workoutDao.getAllWorkoutItems()
    suspend fun insertWorkoutItem(item: WorkoutRoutineItem): Long = workoutDao.insertItem(item)
    suspend fun updateWorkoutItem(item: WorkoutRoutineItem) = workoutDao.updateItem(item)
    suspend fun deleteWorkoutItem(item: WorkoutRoutineItem) = workoutDao.deleteItem(item)

    // Completions
    fun getCompletionsForDate(date: String): Flow<List<ActivityCompletion>> = completionDao.getCompletionsForDate(date)
    suspend fun getCompletionsForDateDirect(date: String): List<ActivityCompletion> = completionDao.getCompletionsForDateDirect(date)
    fun getCompletionsForRange(startDate: String, endDate: String): Flow<List<ActivityCompletion>> =
        completionDao.getCompletionsForDateRange(startDate, endDate)
    suspend fun getCompletionsForRangeDirect(startDate: String, endDate: String): List<ActivityCompletion> =
        completionDao.getCompletionsForDateRangeDirect(startDate, endDate)
    val allCompletions: Flow<List<ActivityCompletion>> = completionDao.getAllCompletions()

    suspend fun setCompletion(
        activityId: Long,
        activityType: String,
        date: String,
        scheduledTime: String,
        completed: Boolean,
        completionTime: String?,
        note: String? = null
    ) {
        val existing = completionDao.getCompletion(activityId, activityType, date)
        val completion = existing?.copy(
            completed = completed,
            completionTime = completionTime,
            note = note ?: existing.note
        ) ?: ActivityCompletion(
            activityId = activityId,
            activityType = activityType,
            date = date,
            scheduledTime = scheduledTime,
            completed = completed,
            completionTime = completionTime,
            note = note
        )
        completionDao.insertOrUpdateCompletion(completion)
    }

    suspend fun deleteCompletion(activityId: Long, activityType: String, date: String) {
        completionDao.deleteCompletion(activityId, activityType, date)
    }

    suspend fun clearHistory() {
        completionDao.clearAllCompletions()
    }

    // Custom Reminders
    val customReminders: Flow<List<CustomReminder>> = customReminderDao.getAllCustomReminders()
    suspend fun insertCustomReminder(reminder: CustomReminder): Long = customReminderDao.insertCustomReminder(reminder)
    suspend fun updateCustomReminder(reminder: CustomReminder) = customReminderDao.updateCustomReminder(reminder)
    suspend fun deleteCustomReminder(reminder: CustomReminder) = customReminderDao.deleteCustomReminder(reminder)
    suspend fun getActiveCustomReminders(): List<CustomReminder> = customReminderDao.getActiveReminders()

    // Settings
    val settings: Flow<AppSettings?> = settingsDao.getSettings()
    suspend fun getSettingsDirect(): AppSettings = settingsDao.getSettingsDirect() ?: DefaultData.getDefaultSettings()
    suspend fun updateSettings(settings: AppSettings) = settingsDao.insertOrUpdate(settings)

    // Reset default plan
    suspend fun resetToDefaultPlan() {
        applyPlanTier(DefaultData.PLAN_LOW)
    }

    // Apply a specific plan tier (LOW, MEDIUM, HIGH, CUSTOM)
    suspend fun applyPlanTier(plan: String) {
        val currentSettings = getSettingsDirect()
        updateSettings(currentSettings.copy(selectedPlan = plan))

        if (plan != DefaultData.PLAN_CUSTOM) {
            val newMeals = DefaultData.getMealsForPlan(plan)
            mealDao.deleteAllMeals()
            mealDao.insertMeals(newMeals)
        }
    }

    // Ensure initial data exists if database was opened before Callback ran
    suspend fun ensureInitialized() {
        val existingMeals = mealDao.getAllMeals().firstOrNull()
        if (existingMeals.isNullOrEmpty()) {
            mealDao.insertMeals(DefaultData.getDefaultMeals())
        }
        val existingExercises = exerciseDao.getAllExercises().firstOrNull()
        if (existingExercises.isNullOrEmpty()) {
            exerciseDao.insertExercises(DefaultData.getDefaultExercises())
        }
        val existingWorkout = workoutDao.getAllWorkoutItems().firstOrNull()
        if (existingWorkout.isNullOrEmpty()) {
            workoutDao.insertItems(DefaultData.getDefaultWorkoutItems())
        }
        val existingSettings = settingsDao.getSettingsDirect()
        if (existingSettings == null) {
            settingsDao.insertOrUpdate(DefaultData.getDefaultSettings())
        }
    }
}
