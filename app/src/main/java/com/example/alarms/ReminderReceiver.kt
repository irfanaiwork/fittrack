package com.example.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.AppDatabase
import com.example.data.entities.ActivityCompletion
import com.example.notifications.NotificationHelper
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.example.fittrack.TRIGGER_ALARM"
        const val ACTION_COMPLETE = "com.example.fittrack.ACTION_COMPLETE"
        const val ACTION_DISMISS_NO = "com.example.fittrack.ACTION_DISMISS_NO"
        const val ACTION_SNOOZE = "com.example.fittrack.ACTION_SNOOZE"
        const val ACTION_STOP = "com.example.fittrack.ACTION_STOP"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val activityId = intent.getLongExtra("ACTIVITY_ID", 0L)
        val activityType = intent.getStringExtra("ACTIVITY_TYPE") ?: "MEAL"
        val scheduledTime = intent.getStringExtra("SCHEDULED_TIME") ?: "08:00"
        val today = DateTimeUtils.getTodayIso()

        when (action) {
            ACTION_TRIGGER_ALARM -> {
                val channelId = intent.getStringExtra("CHANNEL_ID") ?: NotificationHelper.CHANNEL_GENERAL
                val title = intent.getStringExtra("TITLE") ?: "FitTrack Reminder"
                val message = intent.getStringExtra("MESSAGE") ?: "Time for your scheduled activity"
                val snoozeMinutes = intent.getIntExtra("SNOOZE_MINUTES", 5)
                val isSnooze = intent.getBooleanExtra("IS_SNOOZE", false)

                // Check if user already marked it completed for today before showing notification
                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    val existing = db.completionDao().getCompletion(activityId, activityType, today)
                    if (existing?.completed == true) {
                        // Already completed, don't show notification
                        return@launch
                    }

                    // Show notification with sound, vibration, and actions
                    NotificationHelper.showReminderNotification(
                        context = context,
                        notificationId = notificationId,
                        channelId = channelId,
                        title = title,
                        message = message,
                        activityId = activityId,
                        activityType = activityType,
                        scheduledTime = scheduledTime,
                        snoozeMinutes = snoozeMinutes
                    )

                    // Reschedule regular occurrence for tomorrow / next week if not a snooze
                    if (!isSnooze) {
                        when (activityType) {
                            "MEAL" -> {
                                val meal = db.mealDao().getMealById(activityId)
                                if (meal != null && meal.enabled && meal.reminderEnabled) {
                                    AlarmScheduler.scheduleMealReminder(context, meal)
                                }
                            }
                            "EXERCISE" -> {
                                val exercise = db.exerciseDao().getExerciseById(activityId)
                                if (exercise != null && exercise.reminderEnabled) {
                                    AlarmScheduler.scheduleExerciseReminder(context, exercise)
                                }
                            }
                            "CUSTOM" -> {
                                val custom = db.customReminderDao().getCustomReminderById(activityId)
                                if (custom != null && custom.enabled) {
                                    AlarmScheduler.scheduleCustomReminder(context, custom)
                                }
                            }
                        }
                    }
                }
            }

            ACTION_COMPLETE -> {
                NotificationHelper.cancelNotification(context, notificationId)
                AlarmScheduler.cancelSnooze(context, activityId)

                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val completionTime = timeFormat.format(Date())

                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    val existing = db.completionDao().getCompletion(activityId, activityType, today)
                    val completion = existing?.copy(
                        completed = true,
                        completionTime = completionTime
                    ) ?: ActivityCompletion(
                        activityId = activityId,
                        activityType = activityType,
                        date = today,
                        scheduledTime = scheduledTime,
                        completed = true,
                        completionTime = completionTime
                    )
                    db.completionDao().insertOrUpdateCompletion(completion)
                }
            }

            ACTION_DISMISS_NO -> {
                NotificationHelper.cancelNotification(context, notificationId)
                AlarmScheduler.cancelSnooze(context, activityId)

                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getInstance(context)
                    val existing = db.completionDao().getCompletion(activityId, activityType, today)
                    val completion = existing?.copy(
                        completed = false,
                        note = "Marked as incomplete"
                    ) ?: ActivityCompletion(
                        activityId = activityId,
                        activityType = activityType,
                        date = today,
                        scheduledTime = scheduledTime,
                        completed = false,
                        note = "Marked as incomplete"
                    )
                    db.completionDao().insertOrUpdateCompletion(completion)
                }
            }

            ACTION_SNOOZE -> {
                NotificationHelper.cancelNotification(context, notificationId)
                val channelId = intent.getStringExtra("CHANNEL_ID") ?: NotificationHelper.CHANNEL_GENERAL
                val title = intent.getStringExtra("TITLE") ?: "FitTrack Reminder"
                val message = intent.getStringExtra("MESSAGE") ?: "Time for your scheduled activity"
                val snoozeMinutes = intent.getIntExtra("SNOOZE_MINUTES", 5)

                AlarmScheduler.scheduleSnooze(
                    context = context,
                    activityId = activityId,
                    activityType = activityType,
                    title = title,
                    message = message,
                    channelId = channelId,
                    scheduledTime = scheduledTime,
                    snoozeMinutes = snoozeMinutes
                )
            }

            ACTION_STOP -> {
                NotificationHelper.cancelNotification(context, notificationId)
                AlarmScheduler.cancelSnooze(context, activityId)
            }
        }
    }
}
