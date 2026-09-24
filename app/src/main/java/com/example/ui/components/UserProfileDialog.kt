package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Male
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.AppSettings
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.utils.CalorieCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileDialog(
    currentSettings: AppSettings?,
    onSaveProfile: (weightKg: Float, heightCm: Float, age: Int, gender: String, activityLevel: String, goal: String) -> Unit,
    onDismiss: () -> Unit
) {
    var gender by remember { mutableStateOf(if (currentSettings?.userGender.isNullOrEmpty()) "Male" else currentSettings!!.userGender) }
    var ageText by remember { mutableStateOf(if ((currentSettings?.userAge ?: 0) > 0) currentSettings!!.userAge.toString() else "25") }
    var weightText by remember { mutableStateOf(if ((currentSettings?.userWeightKg ?: 0f) > 0f) currentSettings!!.userWeightKg.toString() else "70.0") }
    var heightText by remember { mutableStateOf(if ((currentSettings?.userHeightCm ?: 0f) > 0f) currentSettings!!.userHeightCm.toString() else "175.0") }
    var activityLevel by remember { mutableStateOf(currentSettings?.userActivityLevel ?: "Moderate") }
    var goal by remember { mutableStateOf(currentSettings?.userGoal ?: "Maintain Weight") }

    var activityExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }

    val activityLevels = listOf("Sedentary", "Light", "Moderate", "Active", "Very Active")
    val goals = listOf("Lose Fat / Deficit", "Maintain Weight", "Build Muscle / Bulk")

    // Dynamic real-time preview of BMR & TDEE
    val weightVal = weightText.toFloatOrNull() ?: 0f
    val heightVal = heightText.toFloatOrNull() ?: 0f
    val ageVal = ageText.toIntOrNull() ?: 0
    val previewBmr = CalorieCalculator.calculateBmr(weightVal, heightVal, ageVal, gender)
    val previewTdee = CalorieCalculator.calculateTdee(previewBmr, activityLevel)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("user_profile_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("User Profile & Metrics", fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    Text("For accurate BMR & calories burned calculation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Gender Selection (Male / Female)
                Column {
                    Text("Gender (Male / Female)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Male option
                        val isMale = gender.equals("Male", ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { gender = "Male" }
                                .testTag("profile_gender_male"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isMale) GreenContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isMale) BorderStroke(2.dp, GreenPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Male,
                                    contentDescription = "Male",
                                    tint = if (isMale) GreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Male 👨",
                                    fontWeight = if (isMale) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isMale) GreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Female option
                        val isFemale = gender.equals("Female", ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { gender = "Female" }
                                .testTag("profile_gender_female"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isFemale) GreenContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isFemale) BorderStroke(2.dp, GreenPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Female,
                                    contentDescription = "Female",
                                    tint = if (isFemale) GreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Female 👩",
                                    fontWeight = if (isFemale) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isFemale) GreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Age input
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it },
                    label = { Text("Age (Years)") },
                    placeholder = { Text("e.g. 25") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_age_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Weight input (kg)
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Body Weight (kg)") },
                    placeholder = { Text("e.g. 70.0") },
                    supportingText = { Text("Used to compute exercise burn calories accurately") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_weight_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Height input (cm)
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Height (cm)") },
                    placeholder = { Text("e.g. 175.0 (5'9\" ≈ 175 cm)") },
                    supportingText = { Text("5'7\"=170cm, 5'9\"=175cm, 5'11\"=180cm, 6'0\"=183cm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_height_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Activity Level Dropdown
                ExposedDropdownMenuBox(
                    expanded = activityExpanded,
                    onExpandedChange = { activityExpanded = !activityExpanded }
                ) {
                    OutlinedTextField(
                        value = activityLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Daily Activity Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = activityExpanded,
                        onDismissRequest = { activityExpanded = false }
                    ) {
                        activityLevels.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    activityLevel = item
                                    activityExpanded = false
                                }
                            )
                        }
                    }
                }

                // Goal Dropdown
                ExposedDropdownMenuBox(
                    expanded = goalExpanded,
                    onExpandedChange = { goalExpanded = !goalExpanded }
                ) {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fitness Goal") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = goalExpanded,
                        onDismissRequest = { goalExpanded = false }
                    ) {
                        goals.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    goal = item
                                    goalExpanded = false
                                }
                            )
                        }
                    }
                }

                // Live Preview Card
                if (previewBmr > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, tint = GreenDark, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Estimated Metabolic Output", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GreenDark)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Basal Metabolic Rate (BMR):", fontSize = 12.sp, color = GreenDark)
                                Text("$previewBmr kcal/day", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GreenDark)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Maintenance (TDEE):", fontSize = 12.sp, color = GreenDark)
                                Text("$previewTdee kcal/day", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GreenDark)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toFloatOrNull() ?: 70f
                    val h = heightText.toFloatOrNull() ?: 175f
                    val a = ageText.toIntOrNull() ?: 25
                    onSaveProfile(w, h, a, gender, activityLevel, goal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("profile_save_button")
            ) {
                Text("Save Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
