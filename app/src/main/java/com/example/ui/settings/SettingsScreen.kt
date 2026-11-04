package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import com.example.BuildConfig
import com.example.alarms.AlarmRingtonePlayer
import com.example.ui.components.UserProfileDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.CustomReminder
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import com.example.ui.FitTrackViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.AppCreditFooter
import com.example.ui.components.EditCustomReminderDialog
import com.example.ui.components.EditExerciseDialog
import com.example.ui.components.EditMealDialog
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusError
import com.example.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val meals by viewModel.allMeals.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()
    val customReminders by viewModel.customReminders.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var pendingPlanChange by remember { mutableStateOf<String?>(null) }

    var editingMeal by remember { mutableStateOf<Meal?>(null) }
    var showAddMealDialog by remember { mutableStateOf(false) }

    var editingExercise by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    var editingCustomReminder by remember { mutableStateOf<CustomReminder?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    var snoozeDropdownExpanded by remember { mutableStateOf(false) }
    val snoozeOptions = listOf(5 to "5 minutes", 10 to "10 minutes", 15 to "15 minutes")

    var showProfileDialog by remember { mutableStateOf(false) }
    var ringtoneDropdownExpanded by remember { mutableStateOf(false) }
    var isPreviewPlaying by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_content"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Settings & Routine",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customize your daily diet, exercise, and reminder preferences",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 0: User Body Profile & Calorie Calculation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Body Profile & Calories", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        TextButton(onClick = { showProfileDialog = true }) {
                            Text("Edit")
                        }
                    }

                    val hasProfile = (settings?.userWeightKg ?: 0f) > 0f
                    if (hasProfile) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Weight: ${settings?.userWeightKg} kg", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Height: ${settings?.userHeightCm} cm", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column {
                                Text("Age: ${settings?.userAge} yrs • ${settings?.userGender}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Goal: ${settings?.userGoal}", fontSize = 13.sp, color = GreenDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Text(
                            text = "Set your weight, height, age, and gender to enable personalized daily calorie intake & burn tracking.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Set Up Body Profile")
                        }
                    }
                }
            }
        }

        // Section 1: Diet Budget & Plan Selection
        item {
            val currentPlan = settings?.selectedPlan ?: "LOW"
            val planOptions = listOf(
                Triple("LOW", "Low Budget Plan", "Eggs, Chai, Chana & Daal (Economical & Simple)"),
                Triple("MEDIUM", "Medium Budget Plan", "Oats, Milk, Chicken / Daal, Fruits & Peanuts"),
                Triple("HIGH", "High Budget Plan", "Whey Protein, Salmon / Lean Meat, Greek Yogurt & Nuts"),
                Triple("CUSTOM", "Custom Routine", "User-defined custom diet meals and flexible times")
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_plan_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Diet Budget & Routine Plan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Select pre-set low, medium, high budget, or write your own custom meals",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    planOptions.forEach { (planKey, planTitle, planDesc) ->
                        val isSelected = currentPlan == planKey
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!isSelected) {
                                        pendingPlanChange = planKey
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GreenContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = if (isSelected) BorderStroke(1.5.dp, GreenPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = planTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) GreenDark else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = GreenPrimary
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = androidx.compose.ui.graphics.Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = planDesc,
                                        fontSize = 12.sp,
                                        color = if (isSelected) GreenDark.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = GreenPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Diet & Meals
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val currentPlan = settings?.selectedPlan ?: "LOW"
                            val planLabel = when (currentPlan) {
                                "MEDIUM" -> "Medium Budget"
                                "HIGH" -> "High Budget"
                                "CUSTOM" -> "Custom Diet"
                                else -> "Low Budget"
                            }
                            Column {
                                Text("Diet Schedule (${meals.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Plan: $planLabel", fontSize = 11.sp, color = GreenDark, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        IconButton(onClick = { showAddMealDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Meal", tint = GreenPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    meals.forEach { meal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingMeal = meal }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = meal.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = if (meal.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "${meal.food} • ${meal.quantity}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = if (meal.displayTimeRange.isNotEmpty()) meal.displayTimeRange else DateTimeUtils.format12Hour(meal.time),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Weekly Exercise Schedule
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Weekly Exercise Routine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { showAddExerciseDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise", tint = GreenPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Shortcut to Workout Routine
                    OutlinedButton(
                        onClick = { viewModel.selectTab(ScreenTab.WORKOUT_DETAILS) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View / Edit 30-Min Workout Circuit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    exercises.forEach { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingExercise = exercise }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = DateTimeUtils.getDayOfWeekShortName(exercise.dayOfWeek),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = exercise.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "${exercise.duration} • ${exercise.notes.ifEmpty { "Exercise" }}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = DateTimeUtils.format12Hour(exercise.time),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Custom Reminders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Custom Reminders (${customReminders.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        IconButton(onClick = { showAddCustomDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Custom Reminder", tint = GreenPrimary)
                        }
                    }

                    if (customReminders.isEmpty()) {
                        Text(
                            text = "No custom reminders created yet. Tap + to add one (e.g. walk, stretch, green tea).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        customReminders.forEach { reminder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editingCustomReminder = reminder }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(reminder.title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(
                                        text = "${DateTimeUtils.format12Hour(reminder.time)} • ${reminder.repeatType.lowercase().replaceFirstChar { it.uppercase() }}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Notifications & Alarms
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notifications & Alarms", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Master notification toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("All Notifications", fontWeight = FontWeight.Medium)
                            Text("Play alarm and alert at scheduled times", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings?.notificationEnabled ?: true,
                            onCheckedChange = { isEnabled ->
                                settings?.let { viewModel.updateSettings(it.copy(notificationEnabled = isEnabled)) }
                            }
                        )
                    }

                    // Continuous Call / Alarm Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Call / Alarm Mode", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GreenContainer
                                ) {
                                    Text(
                                        text = "Non-Stop Ringing",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GreenDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Plays continuously like an incoming phone call or loud alarm until answered or completed",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings?.alarmContinuousRinging ?: true,
                            onCheckedChange = { isEnabled ->
                                viewModel.setAlarmContinuousRinging(isEnabled)
                            }
                        )
                    }

                    // Ringtone Selection Dropdown & Preview
                    val selectedRingtone = settings?.alarmSound ?: "Continuous Phone Ring"
                    ExposedDropdownMenuBox(
                        expanded = ringtoneDropdownExpanded,
                        onExpandedChange = { ringtoneDropdownExpanded = !ringtoneDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRingtone,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Alarm & Call Ringtone") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ringtoneDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = ringtoneDropdownExpanded,
                            onDismissRequest = { ringtoneDropdownExpanded = false }
                        ) {
                            for (sound in AlarmRingtonePlayer.AVAILABLE_RINGTONES) {
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(sound)
                                            if (sound == selectedRingtone) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = GreenPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setAlarmSound(sound)
                                        ringtoneDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Audio Ringtone Preview & Test Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isPreviewPlaying) {
                                    AlarmRingtonePlayer.stopPreview()
                                    isPreviewPlaying = false
                                } else {
                                    AlarmRingtonePlayer.playPreview(context, selectedRingtone)
                                    isPreviewPlaying = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPreviewPlaying) "Stop Ring" else "Preview Sound", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (isPreviewPlaying) {
                                    AlarmRingtonePlayer.stopPreview()
                                    isPreviewPlaying = false
                                }
                                viewModel.testTriggerCallAlarm()
                            },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Call Alarm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Default Snooze Duration
                    ExposedDropdownMenuBox(
                        expanded = snoozeDropdownExpanded,
                        onExpandedChange = { snoozeDropdownExpanded = !snoozeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${settings?.defaultSnoozeMinutes ?: 5} minutes",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Default Snooze Duration") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = snoozeDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = snoozeDropdownExpanded,
                            onDismissRequest = { snoozeDropdownExpanded = false }
                        ) {
                            snoozeOptions.forEach { (minutes, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        settings?.let { viewModel.updateSettings(it.copy(defaultSnoozeMinutes = minutes)) }
                                        snoozeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // System Settings Buttons
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Open Android Notification Settings", fontSize = 13.sp)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Open Exact Alarm Settings", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Section 5: Data & Maintenance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Plan & Data Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Plan to Default")
                    }

                    OutlinedButton(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusError)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Progress History")
                    }
                }
            }
        }

        // Section 7: About App & Irfan Gulzar Footer
        item {
            AppCreditFooter()
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Reset Dialog Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Default Plan?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure? Your customized diet and exercise schedule will be replaced with the default plan. (Your completed activity logs will be kept)."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaultPlan()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Reset Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Plan Change Confirmation Dialog
    pendingPlanChange?.let { targetPlan ->
        val planName = when (targetPlan) {
            "LOW" -> "Low Budget Plan"
            "MEDIUM" -> "Medium Budget Plan"
            "HIGH" -> "High Budget Plan"
            "CUSTOM" -> "Custom Routine"
            else -> targetPlan
        }
        AlertDialog(
            onDismissRequest = { pendingPlanChange = null },
            title = { Text("Switch to $planName?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (targetPlan == "CUSTOM") {
                        "Switching to Custom Routine allows you to keep your current meals and freely edit, add, or customize diet items and timings."
                    } else {
                        "Switching to $planName will update your meal routine with preset healthy items for this tier. You can still adjust timings and items anytime. Your completed activity history will be kept."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.selectBudgetPlan(targetPlan)
                        pendingPlanChange = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Switch Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingPlanChange = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear History Dialog Confirmation
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Progress History?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will delete all past activity completion logs and weekly report stats. Your custom meals and exercises will not be deleted."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearProgressHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text("Clear History")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Meal
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

    // Edit Exercise
    if (showAddExerciseDialog || editingExercise != null) {
        EditExerciseDialog(
            initialExercise = editingExercise,
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

    // Edit Custom Reminder
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

    // User Profile Dialog
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

}
