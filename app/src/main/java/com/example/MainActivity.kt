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

    // If onboarding is not completed, show the Onboarding flow
    if (settings != null && !settings!!.onboardingCompleted) {
        OnboardingFlow(
            onFinished = {
                viewModel.completeOnboarding()
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
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant,
            thickness = 1.dp
        )
        NavigationBar(
            modifier = Modifier.testTag("fit_track_bottom_bar"),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            // Home
            NavigationBarItem(
                selected = currentTab == ScreenTab.HOME,
                onClick = { onTabSelected(ScreenTab.HOME) },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (currentTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = {
                    Text(
                        "Home",
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == ScreenTab.HOME) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenDark,
                    selectedTextColor = GreenDark,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_home")
            )

            // Calendar
            NavigationBarItem(
                selected = currentTab == ScreenTab.CALENDAR,
                onClick = { onTabSelected(ScreenTab.CALENDAR) },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (currentTab == ScreenTab.CALENDAR) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                        contentDescription = "Calendar"
                    )
                },
                label = {
                    Text(
                        "Calendar",
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == ScreenTab.CALENDAR) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenDark,
                    selectedTextColor = GreenDark,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_calendar")
            )

            // Report
            NavigationBarItem(
                selected = currentTab == ScreenTab.REPORT,
                onClick = { onTabSelected(ScreenTab.REPORT) },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (currentTab == ScreenTab.REPORT) Icons.Filled.InsertChart else Icons.Outlined.InsertChart,
                        contentDescription = "Report"
                    )
                },
                label = {
                    Text(
                        "Report",
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == ScreenTab.REPORT) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenDark,
                    selectedTextColor = GreenDark,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_report")
            )

            // Settings
            NavigationBarItem(
                selected = currentTab == ScreenTab.SETTINGS,
                onClick = { onTabSelected(ScreenTab.SETTINGS) },
                alwaysShowLabel = true,
                icon = {
                    Icon(
                        imageVector = if (currentTab == ScreenTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = {
                    Text(
                        "Settings",
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == ScreenTab.SETTINGS) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = GreenDark,
                    selectedTextColor = GreenDark,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_tab_settings")
            )
        }
    }
}
