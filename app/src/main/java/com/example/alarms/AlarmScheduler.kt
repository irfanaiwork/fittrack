package com.example.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.database.AppDatabase
import com.example.data.entities.CustomReminder
import com.example.data.entities.Exercise
import com.example.data.entities.Meal
import com.example.notifications.NotificationHelper
import com.example.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AlarmScheduler {
    const val REQUEST_CODE_MEAL_OFFSET = 1000
    const val REQUEST_CODE_EXERCISE_OFFSET = 2000
    const val REQUEST_CODE_CUSTOM_OFFSET = 3000
    const val REQUEST_CODE_SNOOZE_OFFSET = 4000

    fun scheduleMealReminder(context: Context, meal: Meal) {
        if (!meal.enabled || !meal.reminderEnabled) {
            cancelMealReminder(context, meal.id)
            return
        }

        val triggerMillis = DateTimeUtils.computeTriggerMillis(meal.time)
        val requestCode = REQUEST_CODE_MEAL_OFFSET + meal.id.toInt()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_ALARM
            putExtra("NOTIFICATION_ID", requestCode)
            putExtra("CHANNEL_ID", NotificationHelper.CHANNEL_DIET)
            putExtra("ACTIVITY_ID", meal.id)
            putExtra("ACTIVITY_TYPE", "MEAL")
            putExtra("TITLE", "${meal.name} Time")
            putExtra("MESSAGE", "${meal.food} • ${meal.quantity}")
            putExtra("SCHEDULED_TIME", meal.time)
        }

        scheduleAlarm(context, requestCode, triggerMillis, intent)
    }

    fun cancelMealReminder(context: Context, mealId: Long) {
        val requestCode = REQUEST_CODE_MEAL_OFFSET + mealId.toInt()
        cancelAlarm(context, requestCode)
    }

    fun scheduleExerciseReminder(context: Context, exercise: Exercise) {
        if (!exercise.reminderEnabled) {
            cancelExerciseReminder(context, exercise.id)
            return
        }

        val triggerMillis = DateTimeUtils.computeTriggerMillis(exercise.time, exercise.dayOfWeek)
        val requestCode = REQUEST_CODE_EXERCISE_OFFSET + exercise.id.toInt()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_ALARM
            putExtra("NOTIFICATION_ID", requestCode)
            putExtra("CHANNEL_ID", NotificationHelper.CHANNEL_EXERCISE)
            putExtra("ACTIVITY_ID", exercise.id)
            putExtra("ACTIVITY_TYPE", "EXERCISE")
            putExtra("TITLE", "Exercise Time: ${exercise.name}")
            putExtra("MESSAGE", "${exercise.duration} • ${exercise.notes.ifEmpty { "Daily scheduled workout" }}")
            putExtra("SCHEDULED_TIME", exercise.time)
            putExtra("DAY_OF_WEEK", exercise.dayOfWeek)
        }

        scheduleAlarm(context, requestCode, triggerMillis, intent)
    }

    fun cancelExerciseReminder(context: Context, exerciseId: Long) {
        val requestCode = REQUEST_CODE_EXERCISE_OFFSET + exerciseId.toInt()
        cancelAlarm(context, requestCode)
    }

    fun scheduleCustomReminder(context: Context, reminder: CustomReminder) {
        if (!reminder.enabled) {
            cancelCustomReminder(context, reminder.id)
            return
        }

        val triggerMillis = DateTimeUtils.computeTriggerMillis(reminder.time)
        val requestCode = REQUEST_CODE_CUSTOM_OFFSET + reminder.id.toInt()

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_ALARM
            putExtra("NOTIFICATION_ID", requestCode)
            putExtra("CHANNEL_ID", NotificationHelper.CHANNEL_GENERAL)
            putExtra("ACTIVITY_ID", reminder.id)
            putExtra("ACTIVITY_TYPE", "CUSTOM")
            putExtra("TITLE", reminder.title)
            putExtra("MESSAGE", reminder.description.ifEmpty { "Scheduled activity" })
            putExtra("SCHEDULED_TIME", reminder.time)
            putExtra("SNOOZE_MINUTES", reminder.snoozeMinutes)
        }

        scheduleAlarm(context, requestCode, triggerMillis, intent)
    }

    fun cancelCustomReminder(context: Context, reminderId: Long) {
        val requestCode = REQUEST_CODE_CUSTOM_OFFSET + reminderId.toInt()
        cancelAlarm(context, requestCode)
    }

    fun scheduleSnooze(
        context: Context,
        activityId: Long,
        activityType: String,
        title: String,
        message: String,
        channelId: String,
        scheduledTime: String,
        snoozeMinutes: Int
    ) {
        val snoozeMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val requestCode = REQUEST_CODE_SNOOZE_OFFSET + (activityId.toInt() % 1000)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_ALARM
            putExtra("NOTIFICATION_ID", requestCode)
            putExtra("CHANNEL_ID", channelId)
            putExtra("ACTIVITY_ID", activityId)
            putExtra("ACTIVITY_TYPE", activityType)
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
            putExtra("SCHEDULED_TIME", scheduledTime)
            putExtra("SNOOZE_MINUTES", snoozeMinutes)
            putExtra("IS_SNOOZE", true)
        }

        scheduleAlarm(context, requestCode, snoozeMillis, intent)
    }

    fun cancelSnooze(context: Context, activityId: Long) {
        val requestCode = REQUEST_CODE_SNOOZE_OFFSET + (activityId.toInt() % 1000)
        cancelAlarm(context, requestCode)
    }

    private fun scheduleAlarm(context: Context, requestCode: Int, triggerMillis: Long, intent: Intent) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun rescheduleAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val settings = db.settingsDao().getSettingsDirect()
            if (settings?.notificationEnabled == false) return@launch

            val meals = db.mealDao().getMealsForAlarms()
            meals.forEach { scheduleMealReminder(context, it) }

            val exercises = db.exerciseDao().getExercisesForAlarms()
            exercises.forEach { scheduleExerciseReminder(context, it) }

            val custom = db.customReminderDao().getActiveReminders()
            custom.forEach { scheduleCustomReminder(context, it) }
        }
    }
}
