package com.example.ui.workout

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.WorkoutRoutineItem
import com.example.ui.FitTrackViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.AppCreditFooter
import com.example.ui.components.EditWorkoutItemDialog
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val workoutItems by viewModel.workoutItems.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()

    val todayDayOfWeek = remember { DateTimeUtils.getCurrentDayOfWeek() }
    val todayWorkout = allExercises.find { it.dayOfWeek == todayDayOfWeek && it.isWorkout }
    val isWorkoutCompletedToday = todayWorkout?.let { ex ->
        todayCompletions.any { it.activityId == ex.id && it.activityType == "EXERCISE" && it.completed }
    } ?: false

    var editingItem by remember { mutableStateOf<WorkoutRoutineItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("30-Minute Workout Routine", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Default home strength circuit", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectTab(ScreenTab.HOME) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GreenPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise")
            }
        },
        modifier = modifier.testTag("workout_details_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = GreenDark, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Full Body Bodyweight Circuit", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GreenDark)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Perform each exercise in sequence with 45–60 seconds rest between sets. Focus on good form over speed.",
                            fontSize = 13.sp,
                            color = GreenDark.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )

                        if (todayWorkout != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    if (isWorkoutCompletedToday) {
                                        viewModel.markIncomplete(todayWorkout.id, "EXERCISE", todayWorkout.time)
                                    } else {
                                        viewModel.markCompleted(todayWorkout.id, "EXERCISE", todayWorkout.time)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isWorkoutCompletedToday) MaterialTheme.colorScheme.surface else GreenPrimary,
                                    contentColor = if (isWorkoutCompletedToday) GreenDark else MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isWorkoutCompletedToday) "✓ Workout Marked Complete for Today" else "Mark Today's Workout Done",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Circuit Exercises (${workoutItems.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(workoutItems, key = { "workout_item_${it.id}" }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GreenContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${item.orderIndex}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = GreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "${item.sets} • ${item.reps}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (item.duration.isNotEmpty()) {
                                    Text(
                                        text = item.duration,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (item.notes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.notes,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        IconButton(onClick = { editingItem = item }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Exercise",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                AppCreditFooter()
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showAddDialog || editingItem != null) {
        EditWorkoutItemDialog(
            initialItem = editingItem,
            onSave = { item ->
                viewModel.saveWorkoutItem(item)
                showAddDialog = false
                editingItem = null
            },
            onDelete = { item ->
                viewModel.deleteWorkoutItem(item)
                editingItem = null
            },
            onDismiss = {
                showAddDialog = false
                editingItem = null
            }
        )
    }
}
