package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.WorkoutRoutineItem
import com.example.ui.theme.StatusError

@Composable
fun EditWorkoutItemDialog(
    initialItem: WorkoutRoutineItem? = null,
    onSave: (WorkoutRoutineItem) -> Unit,
    onDelete: ((WorkoutRoutineItem) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var sets by remember { mutableStateOf(initialItem?.sets ?: "3 sets") }
    var reps by remember { mutableStateOf(initialItem?.reps ?: "10–15") }
    var duration by remember { mutableStateOf(initialItem?.duration ?: "3 min") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialItem == null) "Add Exercise" else "Edit Exercise",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (initialItem != null && onDelete != null) {
                    IconButton(onClick = { onDelete(initialItem) }) {
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
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name (e.g. Push-ups)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it },
                    label = { Text("Sets (e.g. 3 sets)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it },
                    label = { Text("Reps (e.g. 10–15 reps)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (e.g. 3 min)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Form Notes / Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = (initialItem ?: WorkoutRoutineItem(
                        name = name,
                        sets = sets,
                        reps = reps,
                        duration = duration,
                        notes = notes
                    )).copy(
                        name = name.trim().ifEmpty { "Exercise" },
                        sets = sets.trim(),
                        reps = reps.trim(),
                        duration = duration.trim(),
                        notes = notes.trim()
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
