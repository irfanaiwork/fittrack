package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.FitTrackApp
import com.example.alarms.AlarmScheduler
import com.example.data.entities.ActivityCompletion
import com.example.data.entities.AppSettings
import com.example.data.entities.CustomReminder
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import com.example.data.entities.WorkoutRoutineItem
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ScreenTab {
    HOME,
    CALENDAR,
    REPORT,
    SETTINGS,
    WORKOUT_DETAILS
}

data class ReminderPrompt(
    val activityId: Long,
    val activityType: String, // MEAL, EXERCISE, CUSTOM
    val title: String,
    val subtitle: String,
    val time: String,
    val snoozeMinutes: Int = 5
)

data class TodayActivityItem(
    val id: Long,
    val type: String, // MEAL, EXERCISE, CUSTOM
    val title: String,
    val subtitle: String,
    val time: String, // "08:00"
    val displayTime: String, // "8:00 AM"
    val iconEmoji: String,
    val isCompleted: Boolean,
    val isMissed: Boolean,
    val completionTime: String? = null,
    val rawItem: Any
)

data class WeeklyReportData(
    val startIso: String,
    val endIso: String,
    val dateRangeText: String,
    val totalScheduled: Int,
    val completed: Int,
    val missed: Int,
    val percentage: Int,
    val dietPercentage: Int,
    val exercisePercentage: Int,
    val workoutDays: Int,
    val walkingDays: Int,
    val bestDay: String,
    val lowestDay: String
)

class FitTrackViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as FitTrackApp).repository

    val allMeals = repository.allMeals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExercises = repository.allExercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val workoutItems = repository.workoutItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customReminders = repository.customReminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = repository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _activeReminderPrompt = MutableStateFlow<ReminderPrompt?>(null)
    val activeReminderPrompt: StateFlow<ReminderPrompt?> = _activeReminderPrompt.asStateFlow()

    // Calendar state
    private val _calendarMonth = MutableStateFlow(Calendar.getInstance())
    val calendarMonth: StateFlow<Calendar> = _calendarMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateTimeUtils.getTodayIso())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Weekly Report state
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    // All completions
    val allCompletions = repository.allCompletions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's completions
    val todayCompletions = repository.getCompletionsForDate(DateTimeUtils.getTodayIso())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected date completions
    val selectedDateCompletions = repository.getCompletionsForDate(_selectedDate.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calorie & Metabolic Accounting Summary (Dynamic)
    val calorieSummary: StateFlow<com.example.utils.DailyCalorieSummary> = combine(
        settings,
        allMeals,
        allExercises,
        todayCompletions
    ) { sets, meals, exercises, completions ->
        val completedMealIds = completions.filter { it.activityType == "MEAL" && it.completed }.map { it.activityId }.toSet()
        val completedExerciseIds = completions.filter { it.activityType == "EXERCISE" && it.completed }.map { it.activityId }.toSet()
        val todayDow = DateTimeUtils.getTodayDayOfWeek()
        val todayExercises = exercises.filter { it.dayOfWeek == todayDow }
        com.example.utils.CalorieCalculator.computeDailySummary(
            settings = sets,
            meals = meals,
            completedMealIds = completedMealIds,
            exercises = todayExercises,
            completedExerciseIds = completedExerciseIds
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.utils.CalorieCalculator.computeDailySummary(null, emptyList(), emptySet(), emptyList(), emptySet())
    )

    fun selectTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setReminderPrompt(prompt: ReminderPrompt?) {
        _activeReminderPrompt.value = prompt
    }

    fun setSelectedDate(dateIso: String) {
        _selectedDate.value = dateIso
    }

    fun changeCalendarMonth(diff: Int) {
        val newCal = (_calendarMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, diff)
        }
        _calendarMonth.value = newCal
    }

    fun changeWeekOffset(diff: Int) {
        _weekOffset.value += diff
    }

    fun completeOnboarding() {
        startTrackingToday(
            alarmSound = settings.value?.alarmSound ?: "Continuous Phone Ring",
            continuousRinging = settings.value?.alarmContinuousRinging ?: true
        )
    }

    fun startTrackingToday(alarmSound: String, continuousRinging: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            val today = DateTimeUtils.getTodayIso()
            val updated = current.copy(
                onboardingCompleted = true,
                isTrackingStarted = true,
                trackingStartDate = today,
                alarmSound = alarmSound,
                alarmContinuousRinging = continuousRinging
            )
            repository.updateSettings(updated)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun markCompleted(activityId: Long, activityType: String, scheduledTime: String, date: String = DateTimeUtils.getTodayIso()) {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val nowTime = timeFormat.format(Date())
            repository.setCompletion(
                activityId = activityId,
                activityType = activityType,
                date = date,
                scheduledTime = scheduledTime,
                completed = true,
                completionTime = nowTime
            )
            AlarmScheduler.cancelSnooze(getApplication(), activityId)
            _activeReminderPrompt.value = null
        }
    }

    fun markIncomplete(activityId: Long, activityType: String, scheduledTime: String, reason: String? = null, date: String = DateTimeUtils.getTodayIso()) {
        viewModelScope.launch {
            repository.setCompletion(
                activityId = activityId,
                activityType = activityType,
                date = date,
                scheduledTime = scheduledTime,
                completed = false,
                completionTime = null,
                note = reason
            )
            AlarmScheduler.cancelSnooze(getApplication(), activityId)
            _activeReminderPrompt.value = null
        }
    }

    fun snoozeActivity(prompt: ReminderPrompt, minutes: Int = 5) {
        viewModelScope.launch {
            AlarmScheduler.scheduleSnooze(
                context = getApplication(),
                activityId = prompt.activityId,
                activityType = prompt.activityType,
                title = prompt.title,
                message = prompt.subtitle,
                channelId = if (prompt.activityType == "MEAL") "channel_diet_reminders" else "channel_exercise_reminders",
                scheduledTime = prompt.time,
                snoozeMinutes = minutes
            )
            _activeReminderPrompt.value = null
        }
    }

    // Water intake
    fun incrementWater() {
        addWaterGlasses(1)
    }

    fun decrementWater() {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            val today = DateTimeUtils.getTodayIso()
            val consumed = if (current.waterDate == today && current.waterConsumedGlasses > 0) current.waterConsumedGlasses - 1 else 0
            repository.updateSettings(current.copy(waterConsumedGlasses = consumed, waterDate = today))
        }
    }

    fun addWaterGlasses(count: Int) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            val today = DateTimeUtils.getTodayIso()
            val base = if (current.waterDate == today) current.waterConsumedGlasses else 0
            val consumed = (base + count).coerceAtLeast(0)
            repository.updateSettings(current.copy(waterConsumedGlasses = consumed, waterDate = today))
        }
    }

    fun setExactWaterGlasses(glasses: Int) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            val today = DateTimeUtils.getTodayIso()
            repository.updateSettings(current.copy(waterConsumedGlasses = glasses.coerceAtLeast(0), waterDate = today))
        }
    }

    fun setWaterTargetGlasses(target: Int) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            repository.updateSettings(current.copy(waterTargetGlasses = target.coerceIn(1, 30)))
        }
    }

    fun saveUserProfile(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String
    ) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            repository.updateSettings(
                current.copy(
                    userWeightKg = weightKg,
                    userHeightCm = heightCm,
                    userAge = age,
                    userGender = gender,
                    userActivityLevel = activityLevel,
                    userGoal = goal
                )
            )
        }
    }

    fun setAlarmSound(soundName: String) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            repository.updateSettings(current.copy(alarmSound = soundName))
        }
    }

    fun setAlarmContinuousRinging(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value ?: AppSettings()
            repository.updateSettings(current.copy(alarmContinuousRinging = enabled))
        }
    }

    fun testTriggerCallAlarm() {
        val sound = settings.value?.alarmSound ?: "Continuous Phone Ring"
        val prompt = ReminderPrompt(
            activityId = 9999L,
            activityType = "EXERCISE",
            title = "Daily Workout Call Alert",
            subtitle = "30-minute workout • Scheduled for now",
            time = DateTimeUtils.getCurrentTime24(),
            snoozeMinutes = 5
        )
        setReminderPrompt(prompt)
        com.example.alarms.AlarmRingtonePlayer.startRinging(getApplication(), sound, prompt)
    }

    fun stopAlarmRinging() {
        com.example.alarms.AlarmRingtonePlayer.stopRinging()
    }

    // Meal operations
    fun saveMeal(meal: Meal) {
        viewModelScope.launch {
            if (meal.id == 0L) {
                val newId = repository.insertMeal(meal)
                AlarmScheduler.scheduleMealReminder(getApplication(), meal.copy(id = newId))
            } else {
                repository.updateMeal(meal)
                AlarmScheduler.scheduleMealReminder(getApplication(), meal)
            }
        }
    }

    fun deleteMeal(meal: Meal) {
        viewModelScope.launch {
            AlarmScheduler.cancelMealReminder(getApplication(), meal.id)
            repository.deleteMeal(meal)
        }
    }

    // Exercise operations
    fun saveExercise(exercise: Exercise) {
        viewModelScope.launch {
            if (exercise.id == 0L) {
                val newId = repository.insertExercise(exercise)
                AlarmScheduler.scheduleExerciseReminder(getApplication(), exercise.copy(id = newId))
            } else {
                repository.updateExercise(exercise)
                AlarmScheduler.scheduleExerciseReminder(getApplication(), exercise)
            }
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            AlarmScheduler.cancelExerciseReminder(getApplication(), exercise.id)
            repository.deleteExercise(exercise)
        }
    }

    // Workout Routine item operations
    fun saveWorkoutItem(item: WorkoutRoutineItem) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertWorkoutItem(item)
            } else {
                repository.updateWorkoutItem(item)
            }
        }
    }

    fun deleteWorkoutItem(item: WorkoutRoutineItem) {
        viewModelScope.launch {
            repository.deleteWorkoutItem(item)
        }
    }

    // Custom Reminder operations
    fun saveCustomReminder(reminder: CustomReminder) {
        viewModelScope.launch {
            if (reminder.id == 0L) {
                val newId = repository.insertCustomReminder(reminder)
                AlarmScheduler.scheduleCustomReminder(getApplication(), reminder.copy(id = newId))
            } else {
                repository.updateCustomReminder(reminder)
                AlarmScheduler.scheduleCustomReminder(getApplication(), reminder)
            }
        }
    }

    fun deleteCustomReminder(reminder: CustomReminder) {
        viewModelScope.launch {
            AlarmScheduler.cancelCustomReminder(getApplication(), reminder.id)
            repository.deleteCustomReminder(reminder)
        }
    }

    // Settings operations
    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(newSettings)
            if (newSettings.notificationEnabled) {
                AlarmScheduler.rescheduleAll(getApplication())
            }
        }
    }

    fun selectBudgetPlan(plan: String) {
        viewModelScope.launch {
            repository.applyPlanTier(plan)
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun resetToDefaultPlan() {
        viewModelScope.launch {
            repository.resetToDefaultPlan()
            AlarmScheduler.rescheduleAll(getApplication())
        }
    }

    fun clearProgressHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
