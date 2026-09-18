package com.example.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayItem(
    val dateIso: String,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val totalActivities: Int,
    val completedActivities: Int
)

@Composable
fun CalendarScreen(
    viewModel: FitTrackViewModel,
    modifier: Modifier = Modifier
) {
    val calendarMonth by viewModel.calendarMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allCompletions by viewModel.allCompletions.collectAsState()
    val allMeals by viewModel.allMeals.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()

    val monthYearText = remember(calendarMonth) { DateTimeUtils.formatMonthYear(calendarMonth) }
    val todayIso = remember { DateTimeUtils.getTodayIso() }

    // Prepare grid of days for current month view
    val calendarDays = remember(calendarMonth, allCompletions, selectedDate) {
        val days = mutableListOf<CalendarDayItem>()
        val cal = (calendarMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val currentMonth = cal.get(Calendar.MONTH)

        // Find start offset (Monday = 1, ..., Sunday = 7)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val offset = when (firstDayOfWeek) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        // Days before start of month
        cal.add(Calendar.DAY_OF_MONTH, -offset)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Generate 42 cells (6 rows of 7 days)
        for (i in 0 until 42) {
            val dateIso = isoFormat.format(cal.time)
            val isCurrentMonth = cal.get(Calendar.MONTH) == currentMonth
            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
            val isToday = (dateIso == todayIso)
            val isSelected = (dateIso == selectedDate)

            // Calculate completions for this date
            val completionsForDay = allCompletions.filter { it.date == dateIso }
            val completedCount = completionsForDay.count { it.completed }
            val totalCount = completionsForDay.size

            days.add(
                CalendarDayItem(
                    dateIso = dateIso,
                    dayOfMonth = dayNum,
                    isCurrentMonth = isCurrentMonth,
                    isToday = isToday,
                    isSelected = isSelected,
                    totalActivities = totalCount,
                    completedActivities = completedCount
                )
            )

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        days
    }

    // Selected Date completions and details
    val selectedCompletions = allCompletions.filter { it.date == selectedDate }
    val selectedCompletedCount = selectedCompletions.count { it.completed }
    val selectedTotal = selectedCompletions.size
    val selectedPercentage = if (selectedTotal > 0) (selectedCompletedCount * 100) / selectedTotal else 0

    // Selected day of week for default display
    val selectedCal = remember(selectedDate) {
        val c = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d = sdf.parse(selectedDate)
            if (d != null) c.time = d
        } catch (_: Exception) {}
        c
    }
    val selectedDayOfWeek = remember(selectedCal) {
        when (selectedCal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
    val scheduledExercisesForSelectedDay = allExercises.filter { it.dayOfWeek == selectedDayOfWeek }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_content"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Header with Prev/Next
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "History & Calendar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = monthYearText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.changeCalendarMonth(-1) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }
                    IconButton(onClick = { viewModel.changeCalendarMonth(1) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Calendar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Day of week headers (Mon, Tue, Wed, Thu, Fri, Sat, Sun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayName ->
                            Text(
                                text = dayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 6 rows of 7 days
                    val rows = calendarDays.chunked(7)
                    rows.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { dayItem ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                dayItem.isSelected -> GreenPrimary
                                                dayItem.isToday -> GreenContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (dayItem.isToday && !dayItem.isSelected) 1.5.dp else 0.dp,
                                            color = if (dayItem.isToday && !dayItem.isSelected) GreenPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            viewModel.setSelectedDate(dayItem.dateIso)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${dayItem.dayOfMonth}",
                                            fontSize = 13.sp,
                                            fontWeight = if (dayItem.isSelected || dayItem.isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                dayItem.isSelected -> MaterialTheme.colorScheme.onPrimary
                                                !dayItem.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )

                                        // Status dot / badge indicator
                                        if (dayItem.totalActivities > 0) {
                                            val badgeColor = when {
                                                dayItem.completedActivities == dayItem.totalActivities -> StatusSuccess
                                                dayItem.completedActivities > 0 -> StatusWarning
                                                else -> StatusError
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (dayItem.isSelected) MaterialTheme.colorScheme.onPrimary else badgeColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Legend
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusSuccess))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Done", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusWarning))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Partial", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusError))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Missed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Daily Summary Header for Selected Date
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = GreenDark
                        )
                        Text(
                            text = DateTimeUtils.formatDisplayDate(selectedDate),
                            fontSize = 13.sp,
                            color = GreenDark.copy(alpha = 0.8f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$selectedCompletedCount / $selectedTotal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GreenDark
                        )
                        Text(
                            text = "$selectedPercentage% Completed",
                            fontSize = 12.sp,
                            color = GreenDark
                        )
                    }
                }
            }
        }

        // Logged activities list for selected date
        if (selectedCompletions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📅 No activities logged for this date.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Activities you log or mark complete will appear here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Logged Activities",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(selectedCompletions.size) { index ->
                val item = selectedCompletions[index]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.completed) GreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.completed) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (item.completed) StatusSuccess else StatusError,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${item.activityType}: ${DateTimeUtils.format12Hour(item.scheduledTime)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (item.completed && item.completionTime != null) {
                                    Text(
                                        text = "Logged at ${item.completionTime}",
                                        fontSize = 12.sp,
                                        color = StatusSuccess
                                    )
                                } else if (!item.completed && !item.note.isNullOrEmpty()) {
                                    Text(
                                        text = "Reason: ${item.note}",
                                        fontSize = 12.sp,
                                        color = StatusError
                                    )
                                }
                            }
                        }

                        // Toggle button to adjust past log if needed
                        IconButton(
                            onClick = {
                                if (item.completed) {
                                    viewModel.markIncomplete(item.activityId, item.activityType, item.scheduledTime, date = selectedDate)
                                } else {
                                    viewModel.markCompleted(item.activityId, item.activityType, item.scheduledTime, date = selectedDate)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (item.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle status",
                                tint = if (item.completed) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
