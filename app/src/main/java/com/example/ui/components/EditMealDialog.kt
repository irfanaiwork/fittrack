package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Meal
import com.example.ui.theme.StatusError
import com.example.utils.DateTimeUtils

@Composable
fun EditMealDialog(
    initialMeal: Meal? = null,
    onSave: (Meal) -> Unit,
    onDelete: ((Meal) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialMeal?.name ?: "Breakfast") }
    var food by remember { mutableStateOf(initialMeal?.food ?: "") }
    var quantity by remember { mutableStateOf(initialMeal?.quantity ?: "") }
    var time24 by remember { mutableStateOf(initialMeal?.time ?: "08:00") }
    var displayRange by remember { mutableStateOf(initialMeal?.displayTimeRange ?: "") }
    var reminderEnabled by remember { mutableStateOf(initialMeal?.reminderEnabled ?: true) }
    var mealEnabled by remember { mutableStateOf(initialMeal?.enabled ?: true) }

    val (currentHour, currentMin) = remember(time24) { DateTimeUtils.parseHourMinute(time24) }

    fun showTimePicker() {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                time24 = DateTimeUtils.format24Hour(hourOfDay, minute)
                if (displayRange.isEmpty() || displayRange.contains("AM") || displayRange.contains("PM")) {
                    val endHour = (hourOfDay + 1) % 24
                    displayRange = "${DateTimeUtils.format12Hour(time24)}–${DateTimeUtils.format12Hour(DateTimeUtils.format24Hour(endHour, minute))}"
                }
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
                    text = if (initialMeal == null) "Add Meal" else "Edit Meal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (initialMeal != null && onDelete != null) {
                    IconButton(onClick = { onDelete(initialMeal) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Meal", tint = StatusError)
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
                    label = { Text("Meal Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = food,
                    onValueChange = { food = it },
                    label = { Text("Food Items (e.g. Eggs + Bread)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (e.g. 2 eggs + 1 roti)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Time picker button
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
                    value = displayRange,
                    onValueChange = { displayRange = it },
                    label = { Text("Time Window (e.g. 8:00–9:00 AM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Reminder switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reminder Notification", fontWeight = FontWeight.Medium)
                        Text("Notify when meal is due", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }

                // Active switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active in Schedule", fontWeight = FontWeight.Medium)
                        Text("Show in today's plan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = mealEnabled, onCheckedChange = { mealEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = (initialMeal ?: Meal(
                        name = name,
                        food = food,
                        quantity = quantity,
                        time = time24
                    )).copy(
                        name = name.trim().ifEmpty { "Meal" },
                        food = food.trim(),
                        quantity = quantity.trim(),
                        time = time24,
                        displayTimeRange = displayRange.trim(),
                        reminderEnabled = reminderEnabled,
                        enabled = mealEnabled
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Meal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
