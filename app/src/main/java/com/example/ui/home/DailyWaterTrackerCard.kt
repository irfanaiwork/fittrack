package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyWaterTrackerCard(
    consumedGlasses: Int,
    targetGlasses: Int,
    onAddGlass: (Int) -> Unit,
    onRemoveGlass: () -> Unit,
    onSetExactGlasses: (Int) -> Unit,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val target = targetGlasses.coerceAtLeast(1)
    val fraction = (consumedGlasses.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "water_fraction"
    )
    val percentage = ((consumedGlasses.toFloat() / target.toFloat()) * 100).toInt()

    val consumedMl = consumedGlasses * 250
    val targetMl = target * 250
    val isGoalAchieved = consumedGlasses >= target

    val progressBarColor by animateColorAsState(
        targetValue = when {
            isGoalAchieved -> GreenPrimary
            consumedGlasses >= target / 2 -> Color(0xFF0284C7)
            else -> Color(0xFF38BDF8)
        },
        label = "progress_bar_color"
    )

    val hydrationStatus = when {
        consumedGlasses == 0 -> "Start your day with a fresh glass of water! 💧"
        consumedGlasses < target / 2 -> "Keep hydrating! Sip regularly throughout the day 🥤"
        consumedGlasses < target -> "Almost there! Only ${target - consumedGlasses} more glass${if (target - consumedGlasses > 1) "es" else ""} to your daily target! 🏃"
        else -> "Daily hydration goal achieved! Excellent discipline! 🏆💧"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_water_intake_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title, Target & Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Water Tracker",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Water Intake",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: $target glasses ($targetMl ml)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onEditGoal() }
                        .testTag("water_edit_goal_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Goal",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set Goal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats numbers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$consumedGlasses",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isGoalAchieved) GreenDark else Color(0xFF0284C7)
                    )
                    Text(
                        text = " / $target glasses",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGoalAchieved) GreenContainer else Color(0xFFE0F2FE)
                ) {
                    Text(
                        text = "$percentage% • $consumedMl ml",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalAchieved) GreenDark else Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Linear Progress bar
            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressBarColor,
                trackColor = Color(0xFFE0F2FE),
                strokeCap = StrokeCap.Round
            )

            // Goal Celebration Banner
            AnimatedVisibility(
                visible = isGoalAchieved,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GreenContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎉 Daily Hydration Goal Completed! Outstanding job!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Visual Glasses Grid (Tap on any glass to toggle fill)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 1..target) {
                    val isFilled = i <= consumedGlasses
                    val glassScale by animateFloatAsState(
                        targetValue = if (isFilled) 1.05f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "glass_scale_$i"
                    )

                    Box(
                        modifier = Modifier
                            .scale(glassScale)
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isFilled) Color(0xFFBAE6FD) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .clickable {
                                if (isFilled && i == consumedGlasses) {
                                    onRemoveGlass()
                                } else {
                                    onSetExactGlasses(i)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = "Glass $i",
                            tint = if (isFilled) Color(0xFF0284C7) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hydration Advice text
            Text(
                text = hydrationStatus,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Action Buttons (+1 Glass, +2 Glasses, -1 Glass)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // -1 Glass (Undo)
                OutlinedButton(
                    onClick = { onRemoveGlass() },
                    enabled = consumedGlasses > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("water_minus_one_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove 1 Glass", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("-1", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // +1 Glass (250ml) - Primary Button
                Button(
                    onClick = { onAddGlass(1) },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(42.dp)
                        .testTag("water_plus_one_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add 1 Glass", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+1 (250ml)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // +2 Glasses (500ml / Bottle)
                FilledTonalButton(
                    onClick = { onAddGlass(2) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("water_plus_two_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFE0F2FE),
                        contentColor = Color(0xFF0284C7)
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add 2 Glasses", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("+2 (500ml)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
