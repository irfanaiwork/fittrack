package com.example

import android.app.Application
import com.example.alarms.AlarmScheduler
import com.example.data.database.AppDatabase
import com.example.data.repository.FitTrackRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitTrackApp : Application() {

    lateinit var repository: FitTrackRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        repository = FitTrackRepository(database)

        NotificationHelper.createNotificationChannels(this)

        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureInitialized()
            AlarmScheduler.rescheduleAll(this@FitTrackApp)
        }
    }
}
