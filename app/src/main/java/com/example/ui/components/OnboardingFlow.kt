package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.alarms.AlarmRingtonePlayer
import com.example.data.entities.AppSettings
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusSuccess
import com.example.utils.DateTimeUtils

@Composable
fun OnboardingFlow(
    initialSettings: AppSettings?,
    onStartToday: (alarmSound: String, continuousRinging: Boolean) -> Unit
) {
    val context = LocalContext.current
    var stepIndex by remember { mutableIntStateOf(0) }

    // Sound Selection state
    var selectedSound by remember { mutableStateOf(initialSettings?.alarmSound ?: "Continuous Phone Ring") }
    var continuousRinging by remember { mutableStateOf(initialSettings?.alarmContinuousRinging ?: true) }
    val previewingTone by AlarmRingtonePlayer.previewingTone.collectAsState()

    // Permissions state
    var isNotificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            isNotificationPermissionGranted = granted
        }
    )

    // Stop ringtone preview when leaving onboarding
    DisposableEffect(Unit) {
        onDispose {
            AlarmRingtonePlayer.stopPreview()
        }
    }

    val todayIso = remember { DateTimeUtils.getTodayIso() }
    val formattedToday = remember { DateTimeUtils.formatDisplayDate(todayIso) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / App Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "FitTrack Daily",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GreenDark
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (stepIndex) {
                        0 -> "Step 1 of 3: Permissions & Setup"
                        1 -> "Step 2 of 3: Notification & Alarm Sound"
                        else -> "Step 3 of 3: Start Your Journey"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Animated Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                AnimatedContent(
                    targetState = stepIndex,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "onboarding_step_content"
                ) { targetStep ->
                    when (targetStep) {
                        0 -> StepPermissions(
                            isNotificationGranted = isNotificationPermissionGranted,
                            onRequestNotificationPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                        )
                        1 -> StepSoundCustomization(
                            selectedSound = selectedSound,
                            onSelectSound = {
                                selectedSound = it
                                AlarmRingtonePlayer.stopPreview()
                            },
                            previewingTone = previewingTone,
                            onTogglePreview = { sound ->
                                if (previewingTone == sound) {
                                    AlarmRingtonePlayer.stopPreview()
                                } else {
                                    AlarmRingtonePlayer.playPreview(context, sound)
                                }
                            },
                            continuousRinging = continuousRinging,
                            onToggleContinuousRinging = { continuousRinging = it }
                        )
                        2 -> StepStartToday(
                            formattedDate = formattedToday,
                            selectedSound = selectedSound,
                            isNotificationGranted = isNotificationPermissionGranted,
                            onStartTodayClicked = {
                                AlarmRingtonePlayer.stopPreview()
                                onStartToday(selectedSound, continuousRinging)
                            }
                        )
                    }
                }
            }

            // Bottom Navigation Controls & Indicators
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated step dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    for (i in 0..2) {
                        val isCurrent = i == stepIndex
                        val isPassed = i < stepIndex
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 24.dp else 10.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isCurrent -> GreenPrimary
                                        isPassed -> GreenDark.copy(alpha = 0.6f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                        )
                    }
                }

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (stepIndex > 0) {
                        OutlinedButton(
                            onClick = {
                                AlarmRingtonePlayer.stopPreview()
                                stepIndex--
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    if (stepIndex < 2) {
                        Button(
                            onClick = {
                                AlarmRingtonePlayer.stopPreview()
                                stepIndex++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("onboarding_next_button")
                        ) {
                            Text("Next", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 0: Welcome & Permissions (POST_NOTIFICATIONS and Alarms)
 */
@Composable
private fun StepPermissions(
    isNotificationGranted: Boolean,
    onRequestNotificationPermission: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Icon
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(GreenContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = GreenDark,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to FitTrack Daily",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "FitTrack helps you stay consistent with daily meals, workout sessions, and hydration. Give permissions below to enable on-time reminders.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Notification Permission Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("notification_permission_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNotificationGranted) GreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isNotificationGranted) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isNotificationGranted) GreenPrimary else MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isNotificationGranted) Icons.Default.Check else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (isNotificationGranted) Color.White else GreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Notification Permission",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isNotificationGranted) "Enabled: You'll receive diet & workout alerts" else "Required to ring reminders at scheduled times",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isNotificationGranted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GreenPrimary
                    ) {
                        Text(
                            text = "Granted ✅",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onRequestNotificationPermission,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Allow", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Exact Alarm Guidance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = GreenDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Exact Alarm Scheduling",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Alarms ring directly on schedule even in deep sleep mode",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Step 1: Reminder & Call Sound Selection with Interactive Animated Equalizer
 */
@Composable
private fun StepSoundCustomization(
    selectedSound: String,
    onSelectSound: (String) -> Unit,
    previewingTone: String?,
    onTogglePreview: (String) -> Unit,
    continuousRinging: Boolean,
    onToggleContinuousRinging: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Notification Sound",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Select your preferred tone and preview it with live audio wave",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Continuous Phone Ring Switch Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = GreenContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Continuous Call Alarm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GreenDark
                    )
                    Text(
                        text = "Rings continuously like an incoming phone call until you answer or snooze",
                        fontSize = 11.sp,
                        color = GreenDark.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = continuousRinging,
                    onCheckedChange = onToggleContinuousRinging,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GreenPrimary
                    )
                )
            }
        }

        // List of Sounds
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AlarmRingtonePlayer.ringtoneOptions) { (soundName, soundDesc) ->
                val isSelected = selectedSound == soundName
                val isPlaying = previewingTone == soundName

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSound(soundName) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) GreenContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectSound(soundName) },
                                colors = RadioButtonDefaults.colors(selectedColor = GreenPrimary)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Text(
                                    text = soundName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (isSelected) GreenDark else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = soundDesc,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Preview / Stop Button with Equalizer Wave Animation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isPlaying) {
                                AnimatedEqualizerWave()
                            }

                            IconButton(
                                onClick = { onTogglePreview(soundName) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop Sound" else "Preview Sound",
                                    tint = if (isPlaying) Color.White else GreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated audio equalizer bars bouncing when previewing sound
 */
@Composable
private fun AnimatedEqualizerWave() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.height(24.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h1.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(GreenPrimary)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h2.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(GreenDark)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(h3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(GreenPrimary)
        )
    }
}

/**
 * Step 2: "Start Today" Activation Screen with Pulsing Button Animation
 */
@Composable
private fun StepStartToday(
    formattedDate: String,
    selectedSound: String,
    isNotificationGranted: Boolean,
    onStartTodayClicked: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_button")

    // Pulsing scale animation for the Start Today button
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Glow border alpha animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Rocket Launch Icon with Badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(GreenContainer, GreenPrimary.copy(alpha = 0.2f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = GreenDark,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your Routine Starts Today!",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = GreenContainer,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Starting Date: $formattedDate",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GreenDark,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Schedule Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Scheduled Daily Routine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    Text("4 Daily Meals (Breakfast, Lunch, Snack, Dinner)", fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    Text("2 Daily Exercises (Morning Walk & Evening Workout)", fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.LocalDrink, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    Text("Hydration Tracker (8 glasses daily target)", fontSize = 12.sp)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    Text("Alert Sound: $selectedSound", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Animated Pulsing "Start Today" Button
        Box(
            modifier = Modifier
                .scale(buttonScale)
                .border(
                    width = 2.dp,
                    color = GreenPrimary.copy(alpha = glowAlpha),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Button(
                onClick = onStartTodayClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("start_today_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Start Today",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Tap to activate your schedule and start tracking from today.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
