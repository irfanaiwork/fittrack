package com.example.utils

import com.example.data.entities.AppSettings
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import kotlin.math.roundToInt

data class DailyCalorieSummary(
    val isProfileSet: Boolean,
    val userWeightKg: Float,
    val userHeightCm: Float,
    val userAge: Int,
    val userGender: String,
    val bmr: Int,
    val tdee: Int,
    val totalMealsConsumedCalories: Int,
    val totalMealsPlannedCalories: Int,
    val totalWorkoutBurnedCalories: Int,
    val totalScheduledWorkoutCalories: Int,
    val netCalories: Int,
    val calorieBalance: Int, // Consumed - (BMR + WorkoutBurn)
    val isDeficit: Boolean,
    val statusMessage: String
)

object CalorieCalculator {

    fun isProfileConfigured(settings: AppSettings?): Boolean {
        if (settings == null) return false
        return settings.userWeightKg > 0f &&
                settings.userHeightCm > 0f &&
                settings.userAge > 0 &&
                settings.userGender.isNotBlank()
    }

    /**
     * Mifflin-St Jeor Equation for Basal Metabolic Rate (BMR)
     */
    fun calculateBmr(weightKg: Float, heightCm: Float, age: Int, gender: String): Int {
        if (weightKg <= 0f || heightCm <= 0f || age <= 0) return 0
        val base = (10f * weightKg) + (6.25f * heightCm) - (5f * age)
        val bmr = if (gender.equals("Female", ignoreCase = true)) {
            base - 161f
        } else {
            base + 5f
        }
        return bmr.roundToInt().coerceAtLeast(800)
    }

    /**
     * Total Daily Energy Expenditure based on activity level
     */
    fun calculateTdee(bmr: Int, activityLevel: String): Int {
        val multiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2f
            "light" -> 1.375f
            "active" -> 1.725f
            "very active" -> 1.9f
            else -> 1.55f // Moderate
        }
        return (bmr * multiplier).roundToInt()
    }

    /**
     * Parses duration minutes from string like "30 min", "45 mins", "1 hour"
     */
    fun parseDurationMinutes(duration: String): Int {
        val clean = duration.trim().lowercase()
        return try {
            if (clean.contains("hour") || clean.contains("hr")) {
                val num = clean.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 1f
                (num * 60).roundToInt()
            } else {
                val num = clean.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 30
                num.coerceIn(5, 300)
            }
        } catch (e: Exception) {
            30
        }
    }

    /**
     * Calories burned = MET * BodyWeight(kg) * (Duration in minutes / 60)
     */
    fun calculateExerciseBurn(exercise: Exercise, weightKg: Float): Int {
        val effectiveWeight = if (weightKg > 20f) weightKg else 70f
        val minutes = parseDurationMinutes(exercise.duration)
        val met = if (exercise.metValue > 0f) exercise.metValue else if (exercise.isWorkout) 6.0f else 3.8f
        val burn = met * effectiveWeight * (minutes / 60f)
        return burn.roundToInt().coerceAtLeast(20)
    }

    fun computeDailySummary(
        settings: AppSettings?,
        meals: List<Meal>,
        completedMealIds: Set<Long>,
        exercises: List<Exercise>,
        completedExerciseIds: Set<Long>
    ): DailyCalorieSummary {
        val isConfigured = isProfileConfigured(settings)
        val weight = settings?.userWeightKg ?: 0f
        val height = settings?.userHeightCm ?: 0f
        val age = settings?.userAge ?: 0
        val gender = settings?.userGender ?: ""
        val activityLevel = settings?.userActivityLevel ?: "Moderate"

        val bmr = if (isConfigured) calculateBmr(weight, height, age, gender) else 0
        val tdee = if (isConfigured) calculateTdee(bmr, activityLevel) else 0

        // Diet consumed vs planned
        var consumedCals = 0
        var plannedCals = 0
        for (meal in meals) {
            if (meal.enabled) {
                plannedCals += meal.calories
                if (completedMealIds.contains(meal.id)) {
                    consumedCals += meal.calories
                }
            }
        }

        // Workout burn
        var workoutBurn = 0
        var scheduledBurn = 0
        for (ex in exercises) {
            val burn = calculateExerciseBurn(ex, if (isConfigured) weight else 70f)
            scheduledBurn += burn
            if (completedExerciseIds.contains(ex.id)) {
                workoutBurn += burn
            }
        }

        val netCalories = consumedCals - workoutBurn
        // Balance = Consumed - (BMR + WorkoutBurn)
        val calorieBalance = if (isConfigured) consumedCals - (bmr + workoutBurn) else 0
        val isDeficit = calorieBalance < 0

        val statusMessage = when {
            !isConfigured -> "Set your profile (Weight, Height, Age, Gender) to calculate calories"
            calorieBalance < -500 -> "In Calorie Deficit (${calorieBalance} kcal) — Burning fat! 🔥"
            calorieBalance < 0 -> "Moderate Deficit (${calorieBalance} kcal) — Steady fat burn 🏃"
            calorieBalance in 0..200 -> "Balanced Maintenance (~${calorieBalance} kcal) ⚖️"
            else -> "In Calorie Surplus (+${calorieBalance} kcal) — Energy / Muscle surplus 💪"
        }

        return DailyCalorieSummary(
            isProfileSet = isConfigured,
            userWeightKg = weight,
            userHeightCm = height,
            userAge = age,
            userGender = gender,
            bmr = bmr,
            tdee = tdee,
            totalMealsConsumedCalories = consumedCals,
            totalMealsPlannedCalories = plannedCals,
            totalWorkoutBurnedCalories = workoutBurn,
            totalScheduledWorkoutCalories = scheduledBurn,
            netCalories = netCalories,
            calorieBalance = calorieBalance,
            isDeficit = isDeficit,
            statusMessage = statusMessage
        )
    }
}
