package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Exercise
import com.example.ui.theme.StatusError
import com.example.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseDialog(
    initialExercise: Exercise? = null,
    defaultDayOfWeek: Int = 1,
    onSave: (Exercise) -> Unit,
    onDelete: ((Exercise) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialExercise?.name ?: "30-minute workout") }
    var selectedDay by remember { mutableIntStateOf(initialExercise?.dayOfWeek ?: defaultDayOfWeek) }
    var time24 by remember { mutableStateOf(initialExercise?.time ?: "21:30") }
    var duration by remember { mutableStateOf(initialExercise?.duration ?: "30 min") }
    var notes by remember { mutableStateOf(initialExercise?.notes ?: "") }
    var isWorkout by remember { mutableStateOf(initialExercise?.isWorkout ?: true) }
    var reminderEnabled by remember { mutableStateOf(initialExercise?.reminderEnabled ?: true) }

    var dayDropdownExpanded by remember { mutableStateOf(false) }

    val days = listOf(
        1 to "Monday",
        2 to "Tuesday",
        3 to "Wednesday",
        4 to "Thursday",
        5 to "Friday",
        6 to "Saturday",
        7 to "Sunday"
    )

    val (currentHour, currentMin) = remember(time24) { DateTimeUtils.parseHourMinute(time24) }

    fun showTimePicker() {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                time24 = DateTimeUtils.format24Hour(hourOfDay, minute)
            },
            currentHour,
            currentMin,
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialExercise == null) "Add Exercise Activity" else "Edit Activity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (initialExercise != null && onDelete != null) {
                    IconButton(onClick = { onDelete(initialExercise) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = StatusError)
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Day of Week Dropdown
                ExposedDropdownMenuBox(
                    expanded = dayDropdownExpanded,
                    onExpandedChange = { dayDropdownExpanded = !dayDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = DateTimeUtils.getDayOfWeekName(selectedDay),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Day of the Week") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dayDropdownExpanded,
                        onDismissRequest = { dayDropdownExpanded = false }
                    ) {
                        days.forEach { (dayNum, dayName) ->
                            DropdownMenuItem(
                                text = { Text(dayName) },
                                onClick = {
                                    selectedDay = dayNum
                                    dayDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Activity Name (e.g. 30-min workout, Walk)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedButton(
                    onClick = { showTimePicker() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Time: ${DateTimeUtils.format12Hour(time24)}",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g. 30 min, 45 min)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Routine details") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Count as Strength Workout", fontWeight = FontWeight.Medium)
                        Text("Otherwise counted as walking/recovery", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isWorkout, onCheckedChange = { isWorkout = it })
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reminder Notification", fontWeight = FontWeight.Medium)
                        Text("Notify when activity is due", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = (initialExercise ?: Exercise(
                        name = name,
                        dayOfWeek = selectedDay,
                        time = time24
                    )).copy(
                        name = name.trim().ifEmpty { "Exercise" },
                        dayOfWeek = selectedDay,
                        time = time24,
                        duration = duration.trim(),
                        notes = notes.trim(),
                        isWorkout = isWorkout,
                        reminderEnabled = reminderEnabled,
                        metValue = if (isWorkout) 6.0f else 3.8f
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Exercise")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
