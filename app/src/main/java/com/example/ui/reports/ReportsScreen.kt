package com.example.ui.reports

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FitTrackViewModel
import com.example.ui.components.AppCreditFooter
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.utils.DateTimeUtils

@Composable
fun ReportsScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val weekOffset by viewModel.weekOffset.collectAsState()
    val allCompletions by viewModel.allCompletions.collectAsState()
    val allMeals by viewModel.allMeals.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()

    val (startIso, endIso) = remember(weekOffset) { DateTimeUtils.getWeekDateRange(weekOffset) }
    val weekRangeText = remember(startIso, endIso) { DateTimeUtils.formatWeekRangeDisplay(startIso, endIso) }
    val weekDates = remember(weekOffset) { DateTimeUtils.getWeekDates(weekOffset) }

    // Completions in this week
    val weekCompletions = remember(allCompletions, startIso, endIso) {
        allCompletions.filter { it.date in startIso..endIso }
    }

    // Enabled meals per day = meals count * 7
    val scheduledDietPerDay = allMeals.count { it.enabled }
    val scheduledExercisesCount = allExercises.count { it.reminderEnabled || it.name.isNotEmpty() }
    val totalExpectedActivities = (scheduledDietPerDay * 7) + scheduledExercisesCount

    val totalCompleted = weekCompletions.count { it.completed }
    val totalLogged = weekCompletions.size

    val overallPercentage = if (totalExpectedActivities > 0) {
        ((totalCompleted.toFloat() / totalExpectedActivities.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 0

    // Diet stats
    val dietCompletions = weekCompletions.filter { it.activityType == "MEAL" }
    val dietCompletedCount = dietCompletions.count { it.completed }
    val totalExpectedDiet = scheduledDietPerDay * 7
    val dietPercentage = if (totalExpectedDiet > 0) {
        ((dietCompletedCount.toFloat() / totalExpectedDiet.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 0

    // Exercise stats
    val exerciseCompletions = weekCompletions.filter { it.activityType == "EXERCISE" }
    val exerciseCompletedCount = exerciseCompletions.count { it.completed }
    val totalExpectedExercise = scheduledExercisesCount
    val exercisePercentage = if (totalExpectedExercise > 0) {
        ((exerciseCompletedCount.toFloat() / totalExpectedExercise.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else 0

    // Strength workouts count vs walking days
    val workoutDays = exerciseCompletions.count { comp ->
        comp.completed && allExercises.any { it.id == comp.activityId && it.isWorkout }
    }
    val walkingDays = exerciseCompletions.count { comp ->
        comp.completed && allExercises.any { it.id == comp.activityId && !it.isWorkout }
    }

    // Best Day and Lowest Day
    val dayStats = remember(weekDates, weekCompletions) {
        weekDates.map { (dayNum, dateIso) ->
            val comps = weekCompletions.filter { it.date == dateIso }
            val done = comps.count { it.completed }
            val total = comps.size
            val pct = if (total > 0) (done * 100) / total else 0
            Triple(DateTimeUtils.getDayOfWeekName(dayNum), done, pct)
        }
    }

    val bestDayItem = dayStats.maxByOrNull { it.third }
    val bestDayText = if (bestDayItem != null && bestDayItem.second > 0) "${bestDayItem.first} (${bestDayItem.third}%)" else "None yet"

    val lowestDayItem = dayStats.filter { it.second > 0 || weekCompletions.isNotEmpty() }.minByOrNull { it.third }
    val lowestDayText = if (lowestDayItem != null && weekCompletions.isNotEmpty()) "${lowestDayItem.first} (${lowestDayItem.third}%)" else "None"

    // Animation for emoji
    val infiniteTransition = rememberInfiniteTransition(label = "emoji_anim")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen_content"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Report",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = weekRangeText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.changeWeekOffset(-1) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Week")
                    }
                    IconButton(onClick = { viewModel.changeWeekOffset(1) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week")
                    }
                }
            }
        }

        // Performance Tier Animated Emoji Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        overallPercentage >= 80 -> GreenContainer
                        overallPercentage >= 50 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(scaleAnim)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when {
                                overallPercentage >= 80 -> "🎉"
                                overallPercentage >= 50 -> "🙂"
                                else -> "😔"
                            },
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = when {
                            overallPercentage >= 80 -> "Great week! Keep going!"
                            overallPercentage >= 50 -> "Good progress. You can do even better next week!"
                            else -> "It's okay. Start fresh and focus on the next day."
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = when {
                            overallPercentage >= 80 -> GreenDark
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            overallPercentage >= 80 -> "Outstanding dedication to your diet and workout goals."
                            overallPercentage >= 50 -> "Solid consistency! Keep building your momentum."
                            else -> "Every journey has quiet days. Focus on one meal and one movement."
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Overall Weekly Progress Bar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekly Consistency", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(
                                text = "$totalCompleted activities completed this week",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "$overallPercentage%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            color = GreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (overallPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = GreenPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        // Diet & Exercise Category Cards (2 columns)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Diet Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Diet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("$dietPercentage%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                        Text("$dietCompletedCount completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Exercise Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exercise", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("$exercisePercentage%", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GreenPrimary)
                        Text("$exerciseCompletedCount completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Workout & Walking Days Details
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Workouts count
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏋️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Workouts", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$workoutDays days", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Walks count
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Walks", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$walkingDays days", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Best Day & Lowest Day
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
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
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Best Day", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        Text(bestDayText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusSuccess)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TrendingDown, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lowest Day", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                        Text(lowestDayText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusWarning)
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
