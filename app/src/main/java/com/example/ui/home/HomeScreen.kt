package com.example.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.CustomReminder
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import com.example.ui.FitTrackViewModel
import com.example.ui.ReminderPrompt
import com.example.ui.ScreenTab
import com.example.ui.components.AppCreditFooter
import com.example.ui.components.EditCustomReminderDialog
import com.example.ui.components.EditExerciseDialog
import com.example.ui.components.EditMealDialog
import com.example.ui.components.UserProfileDialog
import com.example.ui.components.WaterGoalDialog
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.utils.CalorieCalculator
import com.example.utils.DateTimeUtils

@Composable
fun HomeScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val meals by viewModel.allMeals.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()
    val customReminders by viewModel.customReminders.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val calorieSummary by viewModel.calorieSummary.collectAsState()

    val todayIso = remember { DateTimeUtils.getTodayIso() }
    val todayDayOfWeek = remember { DateTimeUtils.getCurrentDayOfWeek() }
    val greeting = remember { DateTimeUtils.getGreeting() }
    val formattedDate = remember { DateTimeUtils.formatDisplayDate(todayIso) }

    // Dialog edit states
    var editingMeal by remember { mutableStateOf<Meal?>(null) }
    var showAddMealDialog by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var editingCustomReminder by remember { mutableStateOf<CustomReminder?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showAddMenuDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }

    // Filter enabled meals
    val enabledMeals = meals.filter { it.enabled }
    // Today's exercises
    val todayExercises = exercises.filter { it.dayOfWeek == todayDayOfWeek }
    // Active custom reminders
    val activeCustomReminders = customReminders.filter { it.enabled }

    // Calculate total scheduled activities for today
    val totalScheduled = enabledMeals.size + todayExercises.size + activeCustomReminders.size
    val completedCount = todayCompletions.count { it.completed }
    val progressFraction = if (totalScheduled > 0) (completedCount.toFloat() / totalScheduled.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "progress")

    val waterConsumed = if (settings?.waterDate == todayIso) settings?.waterConsumedGlasses ?: 0 else 0
    val waterTarget = settings?.waterTargetGlasses ?: 8

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen_content"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Greeting, Date, Tagline
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = greeting,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Eat. Move. Track. Improve.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Today's Progress Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_progress_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Progress",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = GreenDark
                                )
                                Text(
                                    text = "$completedCount of $totalScheduled activities completed",
                                    fontSize = 13.sp,
                                    color = GreenDark.copy(alpha = 0.8f)
                                )
                            }
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = GreenPrimary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // Calorie & Workout Burn Accounting Card
            item {
                CalorieBurnTrackerCard(
                    summary = calorieSummary,
                    onOpenProfile = { showProfileDialog = true }
                )
            }

            // Daily Water Intake Tracker Card
            item {
                DailyWaterTrackerCard(
                    consumedGlasses = waterConsumed,
                    targetGlasses = waterTarget,
                    onAddGlass = { viewModel.addWaterGlasses(it) },
                    onRemoveGlass = { viewModel.decrementWater() },
                    onSetExactGlasses = { viewModel.setExactWaterGlasses(it) },
                    onEditGoal = { showWaterGoalDialog = true }
                )
            }

            // Section Header: Today's Schedule
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPlan = settings?.selectedPlan ?: "LOW"
                    val planTag = when (currentPlan) {
                        "MEDIUM" -> "Medium Budget"
                        "HIGH" -> "High Budget"
                        "CUSTOM" -> "Custom Diet"
                        else -> "Low Budget"
                    }
                    Column {
                        Text(
                            text = "Today's Schedule",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Plan: $planTag",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GreenDark
                        )
                    }
                    Text(
                        text = "${DateTimeUtils.getDayOfWeekName(todayDayOfWeek)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Meals
            items(enabledMeals, key = { "meal_${it.id}" }) { meal ->
                val completion = todayCompletions.find { it.activityId == meal.id && it.activityType == "MEAL" }
                val isCompleted = completion?.completed == true
                val isMissed = completion?.completed == false

                ActivityItemCard(
                    title = meal.name,
                    subtitle = "${meal.food} • ${meal.quantity} • ${meal.calories} kcal",
                    timeDisplay = if (meal.displayTimeRange.isNotEmpty()) meal.displayTimeRange else DateTimeUtils.format12Hour(meal.time),
                    iconEmoji = when (meal.name.lowercase()) {
                        "breakfast" -> "🍳"
                        "lunch" -> "🥣"
                        "evening snack", "snack" -> "🥜"
                        "dinner" -> "🍲"
                        else -> "🍽️"
                    },
                    isCompleted = isCompleted,
                    isMissed = isMissed,
                    completionTime = completion?.completionTime,
                    onClick = {
                        viewModel.setReminderPrompt(
                            ReminderPrompt(
                                activityId = meal.id,
                                activityType = "MEAL",
                                title = "${meal.name} Time (${meal.calories} kcal)",
                                subtitle = "${meal.food} • ${meal.quantity}",
                                time = meal.time
                            )
                        )
                    },
                    onToggleComplete = {
                        if (isCompleted) {
                            viewModel.markIncomplete(meal.id, "MEAL", meal.time)
                        } else {
                            viewModel.markCompleted(meal.id, "MEAL", meal.time)
                        }
                    },
                    onEdit = { editingMeal = meal }
                )
            }

            // Today's Exercises
            items(todayExercises, key = { "exercise_${it.id}" }) { exercise ->
                val completion = todayCompletions.find { it.activityId == exercise.id && it.activityType == "EXERCISE" }
                val isCompleted = completion?.completed == true
                val isMissed = completion?.completed == false
                val burnCals = CalorieCalculator.calculateExerciseBurn(
                    exercise,
                    if (calorieSummary.isProfileSet) calorieSummary.userWeightKg else 70f
                )

                Column {
                    ActivityItemCard(
                        title = exercise.name,
                        subtitle = "${exercise.duration} • ~${burnCals} kcal burn • ${exercise.notes.ifEmpty { "Scheduled exercise" }}",
                        timeDisplay = DateTimeUtils.format12Hour(exercise.time),
                        iconEmoji = if (exercise.isWorkout) "🏋️" else "🚶",
                        isCompleted = isCompleted,
                        isMissed = isMissed,
                        completionTime = completion?.completionTime,
                        onClick = {
                            viewModel.setReminderPrompt(
                                ReminderPrompt(
                                    activityId = exercise.id,
                                    activityType = "EXERCISE",
                                    title = "${exercise.name} (Burn ~${burnCals} kcal)",
                                    subtitle = "${exercise.duration} • ${exercise.notes}",
                                    time = exercise.time
                                )
                            )
                        },
                        onToggleComplete = {
                            if (isCompleted) {
                                viewModel.markIncomplete(exercise.id, "EXERCISE", exercise.time)
                            } else {
                                viewModel.markCompleted(exercise.id, "EXERCISE", exercise.time)
                            }
                        },
                        onEdit = { editingExercise = exercise }
                    )

                    if (exercise.isWorkout) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { viewModel.selectTab(ScreenTab.WORKOUT_DETAILS) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Workout Routine & Exercises", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Custom Reminders
            items(activeCustomReminders, key = { "custom_${it.id}" }) { reminder ->
                val completion = todayCompletions.find { it.activityId == reminder.id && it.activityType == "CUSTOM" }
                val isCompleted = completion?.completed == true
                val isMissed = completion?.completed == false

                ActivityItemCard(
                    title = reminder.title,
                    subtitle = reminder.description.ifEmpty { "Custom activity reminder" },
                    timeDisplay = DateTimeUtils.format12Hour(reminder.time),
                    iconEmoji = "⏰",
                    isCompleted = isCompleted,
                    isMissed = isMissed,
                    completionTime = completion?.completionTime,
                    onClick = {
                        viewModel.setReminderPrompt(
                            ReminderPrompt(
                                activityId = reminder.id,
                                activityType = "CUSTOM",
                                title = reminder.title,
                                subtitle = reminder.description,
                                time = reminder.time,
                                snoozeMinutes = reminder.snoozeMinutes
                            )
                        )
                    },
                    onToggleComplete = {
                        if (isCompleted) {
                            viewModel.markIncomplete(reminder.id, "CUSTOM", reminder.time)
                        } else {
                            viewModel.markCompleted(reminder.id, "CUSTOM", reminder.time)
                        }
                    },
                    onEdit = { editingCustomReminder = reminder }
                )
            }

            // App Footer
            item {
                Spacer(modifier = Modifier.height(12.dp))
                AppCreditFooter()
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button to Add Activity / Reminder
        FloatingActionButton(
            onClick = { showAddMenuDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("home_add_fab"),
            containerColor = GreenPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Activity")
        }
    }

    // Add Options Menu Sheet / Dialog
    if (showAddMenuDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddMenuDialog = false },
            title = { Text("Add to FitTrack", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddMenuDialog = false
                                showAddCustomDialog = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⏰", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Custom Activity Reminder", fontWeight = FontWeight.SemiBold)
                                Text("Walk, drink tea, stretch, etc.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddMenuDialog = false
                                showAddMealDialog = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🍽️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("New Meal", fontWeight = FontWeight.SemiBold)
                                Text("Add a meal slot to your daily plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAddMenuDialog = false
                                showAddExerciseDialog = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🏋️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Exercise Session", fontWeight = FontWeight.SemiBold)
                                Text("Schedule a workout or walk", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Edit / Add Meal Dialog
    if (showAddMealDialog || editingMeal != null) {
        EditMealDialog(
            initialMeal = editingMeal,
            onSave = { meal ->
                viewModel.saveMeal(meal)
                showAddMealDialog = false
                editingMeal = null
            },
            onDelete = { meal ->
                viewModel.deleteMeal(meal)
                editingMeal = null
            },
            onDismiss = {
                showAddMealDialog = false
                editingMeal = null
            }
        )
    }

    // Edit / Add Exercise Dialog
    if (showAddExerciseDialog || editingExercise != null) {
        EditExerciseDialog(
            initialExercise = editingExercise,
            defaultDayOfWeek = todayDayOfWeek,
            onSave = { exercise ->
                viewModel.saveExercise(exercise)
                showAddExerciseDialog = false
                editingExercise = null
            },
            onDelete = { exercise ->
                viewModel.deleteExercise(exercise)
                editingExercise = null
            },
            onDismiss = {
                showAddExerciseDialog = false
                editingExercise = null
            }
        )
    }

    // Edit / Add Custom Reminder Dialog
    if (showAddCustomDialog || editingCustomReminder != null) {
        EditCustomReminderDialog(
            initialReminder = editingCustomReminder,
            onSave = { reminder ->
                viewModel.saveCustomReminder(reminder)
                showAddCustomDialog = false
                editingCustomReminder = null
            },
            onDelete = { reminder ->
                viewModel.deleteCustomReminder(reminder)
                editingCustomReminder = null
            },
            onDismiss = {
                showAddCustomDialog = false
                editingCustomReminder = null
            }
        )
    }

    // User Metabolic Profile Dialog (Weight, Height, Age, Gender, Goal)
    if (showProfileDialog) {
        UserProfileDialog(
            currentSettings = settings,
            onSaveProfile = { weight, height, age, gender, activityLevel, goal ->
                viewModel.saveUserProfile(weight, height, age, gender, activityLevel, goal)
                showProfileDialog = false
            },
            onDismiss = { showProfileDialog = false }
        )
    }

    // Water Goal Dialog
    if (showWaterGoalDialog) {
        WaterGoalDialog(
            currentTargetGlasses = waterTarget,
            onSaveGoal = { target ->
                viewModel.setWaterTargetGlasses(target)
                showWaterGoalDialog = false
            },
            onDismiss = { showWaterGoalDialog = false }
        )
    }
}

@Composable
fun ActivityItemCard(
    title: String,
    subtitle: String,
    timeDisplay: String,
    iconEmoji: String,
    isCompleted: Boolean,
    isMissed: Boolean,
    completionTime: String?,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("activity_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> GreenContainer.copy(alpha = 0.5f)
                isMissed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark button on left
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("toggle_complete_${title.lowercase().replace(" ", "_")}")
            ) {
                when {
                    isCompleted -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = StatusSuccess,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    isMissed -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Missed",
                            tint = StatusError,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Mark Complete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = iconEmoji, fontSize = 16.sp)
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = timeDisplay,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isCompleted && completionTime != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Done at $completionTime",
                            fontSize = 11.sp,
                            color = StatusSuccess,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (isMissed) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Incomplete",
                            fontSize = 11.sp,
                            color = StatusError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Edit icon button
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Activity",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
