package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.alarms.ReminderReceiver

object NotificationHelper {
    const val CHANNEL_DIET = "channel_diet_reminders"
    const val CHANNEL_EXERCISE = "channel_exercise_reminders"
    const val CHANNEL_GENERAL = "channel_general_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val dietChannel = NotificationChannel(
                CHANNEL_DIET,
                "Diet Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled meals, breakfast, lunch, snack, and dinner"
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            val exerciseChannel = NotificationChannel(
                CHANNEL_EXERCISE,
                "Exercise Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for scheduled daily workouts and walks"
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "General Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Custom activity reminders and alarms"
                enableVibration(true)
                setSound(defaultSoundUri, audioAttributes)
            }

            notificationManager.createNotificationChannels(listOf(dietChannel, exerciseChannel, generalChannel))
        }
    }

    fun showReminderNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        activityId: Long,
        activityType: String,
        scheduledTime: String,
        snoozeMinutes: Int = 5,
        soundName: String = "Continuous Phone Ring",
        continuousRinging: Boolean = true
    ) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_REMINDER_DIALOG", true)
            putExtra("REMINDER_ACTIVITY_ID", activityId)
            putExtra("REMINDER_ACTIVITY_TYPE", activityType)
            putExtra("REMINDER_TITLE", title)
            putExtra("REMINDER_MESSAGE", message)
            putExtra("REMINDER_TIME", scheduledTime)
            putExtra("IS_CALL_ALARM", true)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // YES, DONE action
        val yesIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_COMPLETE
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("ACTIVITY_ID", activityId)
            putExtra("ACTIVITY_TYPE", activityType)
            putExtra("SCHEDULED_TIME", scheduledTime)
        }
        val yesPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // NO action
        val noIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_DISMISS_NO
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("ACTIVITY_ID", activityId)
            putExtra("ACTIVITY_TYPE", activityType)
            putExtra("SCHEDULED_TIME", scheduledTime)
        }
        val noPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            noIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // SNOOZE action
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SNOOZE
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("ACTIVITY_ID", activityId)
            putExtra("ACTIVITY_TYPE", activityType)
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
            putExtra("CHANNEL_ID", channelId)
            putExtra("SCHEDULED_TIME", scheduledTime)
            putExtra("SNOOZE_MINUTES", snoozeMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 3,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = if (soundName.contains("Phone", ignoreCase = true)) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📞 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\nIncoming FitTrack Call Alert! Tap to answer or mark complete."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(if (continuousRinging) NotificationCompat.CATEGORY_CALL else NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setOngoing(continuousRinging)
            .setSound(soundUri)
            .setFullScreenIntent(tapPendingIntent, true)
            .setContentIntent(tapPendingIntent)
            .setVibrate(longArrayOf(0, 1000, 600, 1000, 600))
            .addAction(0, "✓ ANSWER / DONE", yesPendingIntent)
            .addAction(0, "✕ DECLINE", noPendingIntent)
            .addAction(0, "🔁 SNOOZE ${snoozeMinutes}M", snoozePendingIntent)

        try {
            val notification = builder.build()
            if (continuousRinging) {
                // Insistent flag repeats sound continuously until touched
                notification.flags = notification.flags or android.app.Notification.FLAG_INSISTENT
            }
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission might not be granted
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}
