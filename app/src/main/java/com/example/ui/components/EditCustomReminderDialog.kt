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
import com.example.data.entities.CustomReminder
import com.example.ui.theme.StatusError
import com.example.utils.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomReminderDialog(
    initialReminder: CustomReminder? = null,
    onSave: (CustomReminder) -> Unit,
    onDelete: ((CustomReminder) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var description by remember { mutableStateOf(initialReminder?.description ?: "") }
    var time24 by remember { mutableStateOf(initialReminder?.time ?: "18:30") }
    var repeatType by remember { mutableStateOf(initialReminder?.repeatType ?: "DAILY") }
    var enabled by remember { mutableStateOf(initialReminder?.enabled ?: true) }
    var snoozeEnabled by remember { mutableStateOf(initialReminder?.snoozeEnabled ?: true) }
    var snoozeMinutes by remember { mutableIntStateOf(initialReminder?.snoozeMinutes ?: 5) }

    var repeatDropdownExpanded by remember { mutableStateOf(false) }
    val repeatOptions = listOf(
        "DAILY" to "Daily",
        "WEEKLY" to "Weekly",
        "ONCE" to "Once"
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
                    text = if (initialReminder == null) "Add Custom Reminder" else "Edit Reminder",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                if (initialReminder != null && onDelete != null) {
                    IconButton(onClick = { onDelete(initialReminder) }) {
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Name (e.g. Take a walk)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Notes") },
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

                ExposedDropdownMenuBox(
                    expanded = repeatDropdownExpanded,
                    onExpandedChange = { repeatDropdownExpanded = !repeatDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = repeatOptions.firstOrNull { it.first == repeatType }?.second ?: "Daily",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repeatDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = repeatDropdownExpanded,
                        onDismissRequest = { repeatDropdownExpanded = false }
                    ) {
                        repeatOptions.forEach { (typeKey, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    repeatType = typeKey
                                    repeatDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reminder Enabled", fontWeight = FontWeight.Medium)
                        Text("Receive alert at scheduled time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Snooze Enabled", fontWeight = FontWeight.Medium)
                        Text("Allow 5-min snooze", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = snoozeEnabled, onCheckedChange = { snoozeEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = (initialReminder ?: CustomReminder(
                        title = title,
                        time = time24
                    )).copy(
                        title = title.trim().ifEmpty { "Activity" },
                        description = description.trim(),
                        time = time24,
                        repeatType = repeatType,
                        enabled = enabled,
                        snoozeEnabled = snoozeEnabled,
                        snoozeMinutes = snoozeMinutes
                    )
                    onSave(updated)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
