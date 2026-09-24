package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.ReminderPrompt
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusWarning
import com.example.utils.DateTimeUtils

@Composable
fun IncomingCallAlarmDialog(
    prompt: ReminderPrompt,
    onAnswerDone: () -> Unit,
    onSnooze: (minutes: Int) -> Unit,
    onDeclineDismiss: () -> Unit,
    soundName: String = "Continuous Phone Ring"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Dialog(
        onDismissRequest = onDeclineDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("incoming_call_alarm_dialog"),
            color = Color(0xFF101913) // Dark sleek backdrop for call alert
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Indicator: Incoming Call / Alarm badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GreenPrimary.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .scale(scalePulse)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FITTRACK ALARM RINGING...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Continuous Call Alert",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // Middle: Big Pulsing Phone / Activity Avatar & Details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Concentric pulsing ripples
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer ripple
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(scalePulse)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.15f * alphaPulse))
                        )
                        // Middle ripple
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scalePulse * 0.95f)
                                .clip(CircleShape)
                                .background(GreenPrimary.copy(alpha = 0.25f * alphaPulse))
                        )
                        // Core avatar
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(GreenPrimary)
                                .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val emoji = when (prompt.activityType.uppercase()) {
                                "MEAL" -> "🍳"
                                "EXERCISE" -> "🏋️"
                                else -> "⏰"
                            }
                            Text(
                                text = emoji,
                                fontSize = 42.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = prompt.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )

                    if (prompt.subtitle.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = prompt.subtitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Scheduled at ${DateTimeUtils.format12Hour(prompt.time)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                // Bottom Call / Alarm Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Green "ANSWER / DONE" button
                    Button(
                        onClick = onAnswerDone,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("alarm_call_accept_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "✓ DONE / I'M DOING IT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SNOOZE 5 MIN
                        OutlinedButton(
                            onClick = { onSnooze(prompt.snoozeMinutes) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("alarm_call_snooze_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusWarning),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusWarning)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SNOOZE ${prompt.snoozeMinutes}M",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // DECLINE / DISMISS
                        Button(
                            onClick = onDeclineDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("alarm_call_decline_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DISMISS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
