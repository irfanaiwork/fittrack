package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarms.AlarmRingtonePlayer
import com.example.ui.FitTrackViewModel
import com.example.ui.ReminderPrompt
import com.example.ui.ScreenTab
import com.example.ui.calendar.CalendarScreen
import com.example.ui.components.IncomingCallAlarmDialog
import com.example.ui.components.OnboardingFlow
import com.example.ui.components.ReminderDialog
import com.example.ui.components.UpdateAvailableDialog
import com.example.updates.GitHubUpdateChecker
import com.example.ui.home.HomeScreen
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.reports.ReportsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.workout.WorkoutDetailsScreen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: FitTrackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntentExtras(intent)

        setContent {
            MyApplicationTheme {
                FitTrackAppRoot(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntentExtras(intent)
    }

    private fun handleIntentExtras(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra("OPEN_REMINDER_DIALOG", false)) {
            val activityId = intent.getLongExtra("REMINDER_ACTIVITY_ID", 0L)
            val activityType = intent.getStringExtra("REMINDER_ACTIVITY_TYPE") ?: "MEAL"
            val title = intent.getStringExtra("REMINDER_TITLE") ?: "FitTrack Reminder"
            val message = intent.getStringExtra("REMINDER_MESSAGE") ?: ""
            val scheduledTime = intent.getStringExtra("REMINDER_TIME") ?: "08:00"

            viewModel.setReminderPrompt(
                ReminderPrompt(
                    activityId = activityId,
                    activityType = activityType,
                    title = title,
                    subtitle = message,
                    time = scheduledTime
                )
            )
        }
    }
}

@Composable
fun FitTrackAppRoot(viewModel: FitTrackViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val activeReminderPrompt by viewModel.activeReminderPrompt.collectAsState()

    val isAlarmRinging by AlarmRingtonePlayer.isRinging.collectAsState()
    val ringingPrompt by AlarmRingtonePlayer.ringingPrompt.collectAsState()
    val currentAlertPrompt = activeReminderPrompt ?: ringingPrompt
    val continuousRinging = settings?.alarmContinuousRinging ?: true

    val activeUpdatePrompt by viewModel.activeUpdatePrompt.collectAsState()

    val isTrackingStarted = settings?.isTrackingStarted == true && settings?.onboardingCompleted == true

    // If tracking is not yet started, show the interactive Onboarding & Start Today flow
    if (settings != null && !isTrackingStarted) {
        OnboardingFlow(
            initialSettings = settings,
            onStartToday = { soundName, continuousRinging ->
                viewModel.startTrackingToday(soundName, continuousRinging)
            }
        )
        return
    }

    // Call / Alarm or Active Reminder Dialog
    currentAlertPrompt?.let { prompt ->
        if (isAlarmRinging || continuousRinging) {
            IncomingCallAlarmDialog(
                prompt = prompt,
                soundName = settings?.alarmSound ?: "Continuous Phone Ring",
                onAnswerDone = {
                    viewModel.stopAlarmRinging()
                    viewModel.markCompleted(prompt.activityId, prompt.activityType, prompt.time)
                    viewModel.setReminderPrompt(null)
                },
                onSnooze = { minutes ->
                    viewModel.stopAlarmRinging()
                    viewModel.snoozeActivity(prompt, minutes)
                    viewModel.setReminderPrompt(null)
                },
                onDeclineDismiss = {
                    viewModel.stopAlarmRinging()
                    viewModel.setReminderPrompt(null)
                }
            )
        } else {
            ReminderDialog(
                prompt = prompt,
                onYesDone = {
                    viewModel.markCompleted(prompt.activityId, prompt.activityType, prompt.time)
                },
                onNo = { reason ->
                    viewModel.markIncomplete(prompt.activityId, prompt.activityType, prompt.time, reason = reason)
                },
                onSnooze = { minutes ->
                    viewModel.snoozeActivity(prompt, minutes)
                },
                onDismiss = {
                    viewModel.setReminderPrompt(null)
                }
            )
        }
    }

    // In-App Update Prompt Dialog (Triggered from GitHub release detection or manual check)
    activeUpdatePrompt?.let { updateInfo ->
        UpdateAvailableDialog(
            updateInfo = updateInfo,
            onDownloadUpdate = {
                GitHubUpdateChecker.openUpdateLink(context, updateInfo)
            },
            onViewOnGitHub = {
                GitHubUpdateChecker.openReleasePage(context, updateInfo)
            },
            onDismiss = {
                viewModel.dismissUpdatePrompt(remindLater = false)
            },
            onRemindLater = {
                viewModel.dismissUpdatePrompt(remindLater = true)
            }
        )
    }

    // Handle system back gesture
    BackHandler(enabled = currentTab == ScreenTab.WORKOUT_DETAILS) {
        viewModel.selectTab(ScreenTab.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentTab != ScreenTab.WORKOUT_DETAILS) {
                FitTrackBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                ScreenTab.HOME -> HomeScreen(viewModel = viewModel)
                ScreenTab.CALENDAR -> CalendarScreen(viewModel = viewModel)
                ScreenTab.REPORT -> ReportsScreen(viewModel = viewModel)
                ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                ScreenTab.WORKOUT_DETAILS -> WorkoutDetailsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FitTrackBottomBar(
    currentTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("fit_track_bottom_bar"),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FitTrackNavItem(
                tab = ScreenTab.HOME,
                currentTab = currentTab,
                iconSelected = Icons.Filled.Home,
                iconUnselected = Icons.Outlined.Home,
                label = "Home",
                testTag = "nav_tab_home",
                onClick = { onTabSelected(ScreenTab.HOME) }
            )
            FitTrackNavItem(
                tab = ScreenTab.CALENDAR,
                currentTab = currentTab,
                iconSelected = Icons.Filled.CalendarMonth,
                iconUnselected = Icons.Outlined.CalendarMonth,
                label = "Calendar",
                testTag = "nav_tab_calendar",
                onClick = { onTabSelected(ScreenTab.CALENDAR) }
            )
            FitTrackNavItem(
                tab = ScreenTab.REPORT,
                currentTab = currentTab,
                iconSelected = Icons.Filled.InsertChart,
                iconUnselected = Icons.Outlined.InsertChart,
                label = "Report",
                testTag = "nav_tab_report",
                onClick = { onTabSelected(ScreenTab.REPORT) }
            )
            FitTrackNavItem(
                tab = ScreenTab.SETTINGS,
                currentTab = currentTab,
                iconSelected = Icons.Filled.Settings,
                iconUnselected = Icons.Outlined.Settings,
                label = "Settings",
                testTag = "nav_tab_settings",
                onClick = { onTabSelected(ScreenTab.SETTINGS) }
            )
        }
    }
}

@Composable
private fun RowScope.FitTrackNavItem(
    tab: ScreenTab,
    currentTab: ScreenTab,
    iconSelected: ImageVector,
    iconUnselected: ImageVector,
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    val isSelected = currentTab == tab
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Tactile animated scale on hover and selection
    val scale by animateFloatAsState(
        targetValue = when {
            isHovered -> 1.08f
            isSelected -> 1.02f
            else -> 1.0f
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_item_scale"
    )

    // Pill background color
    val pillBackground by animateColorAsState(
        targetValue = when {
            isSelected -> GreenContainer
            isHovered -> GreenContainer.copy(alpha = 0.55f)
            else -> Color.Transparent
        },
        label = "nav_pill_bg"
    )

    // Content color: high-contrast green for selected/hovered, theme variant for normal
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected || isHovered -> GreenDark
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "nav_content_color"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .testTag(testTag)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = GreenPrimary),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .padding(vertical = 2.dp)
        ) {
            // Icon Pill Container
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(pillBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) iconSelected else iconUnselected,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Label Text: ALWAYS EXPLICITLY DRAWN with full visibility and high contrast
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected || isHovered) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
