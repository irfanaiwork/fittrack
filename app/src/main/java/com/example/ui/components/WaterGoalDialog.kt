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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import kotlin.math.roundToInt

@Composable
fun WaterGoalDialog(
    currentTargetGlasses: Int,
    onSaveGoal: (glasses: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var target by remember { mutableIntStateOf(currentTargetGlasses.coerceIn(4, 20)) }
    val presets = listOf(6, 8, 10, 12, 14)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("water_goal_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalDrink,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Daily Hydration Goal", fontWeight = FontWeight.Bold, fontSize = 19.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select your daily target number of glasses. Staying hydrated boosts metabolism and energy!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Current selection highlight card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$target Glasses",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenDark
                        )
                        Text(
                            text = "≈ ${(target * 250) / 1000f} Liters (${target * 250} ml / day)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = GreenDark.copy(alpha = 0.85f)
                        )
                    }
                }

                // Slider
                Slider(
                    value = target.toFloat(),
                    onValueChange = { target = it.roundToInt() },
                    valueRange = 4f..18f,
                    steps = 13,
                    modifier = Modifier.fillMaxWidth().testTag("water_goal_slider")
                )

                // Quick Preset Chips
                Text("Quick Presets:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        val isSelected = target == preset
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { target = preset }
                        ) {
                            Text(
                                text = "$preset",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveGoal(target) },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("water_goal_save_button")
            ) {
                Text("Set Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
