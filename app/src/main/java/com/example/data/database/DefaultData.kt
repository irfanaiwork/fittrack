package com.example.data.database

import com.example.data.entities.AppSettings
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import com.example.data.entities.WorkoutRoutineItem

object DefaultData {
    const val PLAN_LOW = "LOW"
    const val PLAN_MEDIUM = "MEDIUM"
    const val PLAN_HIGH = "HIGH"
    const val PLAN_CUSTOM = "CUSTOM"

    fun getLowBudgetMeals(): List<Meal> = listOf(
        Meal(
            id = 1,
            name = "Breakfast",
            food = "Eggs + Bread + Chai",
            quantity = "2 eggs + 2 bread slices + 1 cup chai, kam cheeni",
            time = "08:00",
            displayTimeRange = "8:00–9:00 AM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 1,
            calories = 360
        ),
        Meal(
            id = 2,
            name = "Lunch",
            food = "Chana + Roti",
            quantity = "1 bowl chana + 1 roti if needed",
            time = "13:00",
            displayTimeRange = "1:00–2:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 2,
            calories = 460
        ),
        Meal(
            id = 3,
            name = "Evening Snack",
            food = "Light Snack",
            quantity = "½ bowl roasted chana OR water if not hungry",
            time = "17:00",
            displayTimeRange = "5:00–6:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 3,
            calories = 160
        ),
        Meal(
            id = 4,
            name = "Dinner",
            food = "Daal / Sabzi + Roti",
            quantity = "1 bowl + 1–2 roti",
            time = "20:00",
            displayTimeRange = "8:00–9:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 4,
            calories = 420
        )
    )

    fun getMediumBudgetMeals(): List<Meal> = listOf(
        Meal(
            id = 1,
            name = "Breakfast",
            food = "Eggs / Omelette + Oats / Brown Bread + Milk / Chai",
            quantity = "2–3 eggs + 1 bowl oatmeal or 2 brown bread slices + 1 glass milk",
            time = "08:00",
            displayTimeRange = "8:00–9:00 AM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 1,
            calories = 480
        ),
        Meal(
            id = 2,
            name = "Lunch",
            food = "Chicken Curry / Daal + Brown Rice / Roti + Salad",
            quantity = "150g chicken breast or 1 bowl daal + 1 cup rice / 1 roti + fresh cucumber salad",
            time = "13:00",
            displayTimeRange = "1:00–2:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 2,
            calories = 560
        ),
        Meal(
            id = 3,
            name = "Evening Snack",
            food = "Fruit + Roasted Peanuts / Green Tea",
            quantity = "1 apple/banana + handful peanuts + green tea without sugar",
            time = "17:00",
            displayTimeRange = "5:00–6:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 3,
            calories = 220
        ),
        Meal(
            id = 4,
            name = "Dinner",
            food = "Grilled Chicken / Paneer / Mixed Sabzi + Roti",
            quantity = "1 bowl sabzi / grilled chicken + 1 whole wheat roti + yogurt raita",
            time = "20:00",
            displayTimeRange = "8:00–9:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 4,
            calories = 480
        )
    )

    fun getHighBudgetMeals(): List<Meal> = listOf(
        Meal(
            id = 1,
            name = "Breakfast",
            food = "Avocado / Omelette + Greek Yogurt + Berries & Whey",
            quantity = "3 egg whites + 1 whole egg + whole grain toast or avocado + 1 cup Greek yogurt with berries",
            time = "08:00",
            displayTimeRange = "8:00–9:00 AM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 1,
            calories = 520
        ),
        Meal(
            id = 2,
            name = "Lunch",
            food = "Grilled Salmon / Chicken Breast + Quinoa + Steamed Broccoli",
            quantity = "200g grilled salmon or chicken breast + 1 cup cooked quinoa + olive oil dressed vegetables",
            time = "13:00",
            displayTimeRange = "1:00–2:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 2,
            calories = 620
        ),
        Meal(
            id = 3,
            name = "Evening Snack",
            food = "Mixed Tree Nuts + Protein Shake / Fresh Juice",
            quantity = "Handful almonds, walnuts & cashews + 1 scoop whey isolate shake",
            time = "17:00",
            displayTimeRange = "5:00–6:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 3,
            calories = 280
        ),
        Meal(
            id = 4,
            name = "Dinner",
            food = "Steak / Grilled Fish + Sweet Potato + Asparagus",
            quantity = "180g lean steak or white fish fillet + 1 baked sweet potato + grilled asparagus & salad",
            time = "20:00",
            displayTimeRange = "8:00–9:00 PM",
            enabled = true,
            reminderEnabled = true,
            orderIndex = 4,
            calories = 540
        )
    )

    fun getMealsForPlan(plan: String): List<Meal> = when (plan.uppercase()) {
        PLAN_MEDIUM -> getMediumBudgetMeals()
        PLAN_HIGH -> getHighBudgetMeals()
        else -> getLowBudgetMeals()
    }

    fun getDefaultMeals(): List<Meal> = getLowBudgetMeals()

    fun getDefaultExercises(): List<Exercise> = listOf(
        Exercise(
            id = 1,
            name = "30-minute workout",
            dayOfWeek = 1, // Monday
            time = "21:30",
            duration = "30 min",
            notes = "Warm-up, Squats, Push-ups, Lunges, Rows, Glute Bridges, Plank",
            reminderEnabled = true,
            isWorkout = true,
            metValue = 6.0f
        ),
        Exercise(
            id = 2,
            name = "30-minute workout",
            dayOfWeek = 2, // Tuesday
            time = "21:30",
            duration = "30 min",
            notes = "Strength & endurance bodyweight workout",
            reminderEnabled = true,
            isWorkout = true,
            metValue = 6.0f
        ),
        Exercise(
            id = 3,
            name = "Walk",
            dayOfWeek = 3, // Wednesday
            time = "18:00",
            duration = "30 min",
            notes = "Brisk outdoor or treadmill walk",
            reminderEnabled = true,
            isWorkout = false,
            metValue = 3.8f
        ),
        Exercise(
            id = 4,
            name = "30-minute workout",
            dayOfWeek = 4, // Thursday
            time = "21:30",
            duration = "30 min",
            notes = "Full body bodyweight routine",
            reminderEnabled = true,
            isWorkout = true,
            metValue = 6.0f
        ),
        Exercise(
            id = 5,
            name = "Walk",
            dayOfWeek = 5, // Friday
            time = "18:00",
            duration = "30 min",
            notes = "Relaxing evening walk",
            reminderEnabled = true,
            isWorkout = false,
            metValue = 3.8f
        ),
        Exercise(
            id = 6,
            name = "30-minute workout",
            dayOfWeek = 6, // Saturday
            time = "10:30",
            duration = "30 min",
            notes = "Morning strength & core workout",
            reminderEnabled = true,
            isWorkout = true,
            metValue = 6.0f
        ),
        Exercise(
            id = 7,
            name = "Walk / Rest",
            dayOfWeek = 7, // Sunday
            time = "17:00",
            duration = "30 min",
            notes = "Active recovery walk or light stretch",
            reminderEnabled = true,
            isWorkout = false,
            metValue = 3.8f
        )
    )

    fun getDefaultWorkoutItems(): List<WorkoutRoutineItem> = listOf(
        WorkoutRoutineItem(
            id = 1,
            name = "Warm-up",
            sets = "1 set",
            reps = "Light",
            duration = "5 minutes",
            notes = "Joint rotations, gentle jumping jacks, dynamic stretches",
            orderIndex = 1
        ),
        WorkoutRoutineItem(
            id = 2,
            name = "Bodyweight Squats",
            sets = "3 sets",
            reps = "10–15",
            duration = "3 min",
            notes = "Keep chest tall, knees tracking over toes",
            orderIndex = 2
        ),
        WorkoutRoutineItem(
            id = 3,
            name = "Wall / Inclined Push-ups",
            sets = "3 sets",
            reps = "8–12",
            duration = "3 min",
            notes = "Solid plank posture, controlled lower and push",
            orderIndex = 3
        ),
        WorkoutRoutineItem(
            id = 4,
            name = "Lunges",
            sets = "2 sets",
            reps = "8–10 each leg",
            duration = "3 min",
            notes = "Alternate legs, keep balance and tight core",
            orderIndex = 4
        ),
        WorkoutRoutineItem(
            id = 5,
            name = "Backpack Rows",
            sets = "3 sets",
            reps = "10–15",
            duration = "3 min",
            notes = "Flat back, hinge at hips, pull elbows back",
            orderIndex = 5
        ),
        WorkoutRoutineItem(
            id = 6,
            name = "Glute Bridges",
            sets = "3 sets",
            reps = "12–15",
            duration = "2 min",
            notes = "Feet flat, drive through heels, pause at top",
            orderIndex = 6
        ),
        WorkoutRoutineItem(
            id = 7,
            name = "Plank",
            sets = "2–3 sets",
            reps = "Hold",
            duration = "20–30 seconds",
            notes = "Straight line head to heels, breathe steadily",
            orderIndex = 7
        )
    )

    fun getDefaultSettings(): AppSettings = AppSettings(
        id = 1,
        notificationEnabled = true,
        defaultSnoozeMinutes = 5,
        alarmSound = "Continuous Phone Ring",
        onboardingCompleted = false,
        waterTargetGlasses = 8,
        waterConsumedGlasses = 0,
        waterDate = "",
        selectedPlan = PLAN_LOW,
        userWeightKg = 0f,
        userHeightCm = 0f,
        userAge = 0,
        userGender = "",
        userActivityLevel = "Moderate",
        userGoal = "Maintain Weight",
        dailyCalorieTarget = 2000,
        alarmContinuousRinging = true
    )
}
