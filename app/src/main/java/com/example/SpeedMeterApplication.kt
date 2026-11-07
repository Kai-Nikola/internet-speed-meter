package com.example

import android.app.Application
import com.example.data.preferences.SpeedMeterPreferences
import com.example.data.repository.SpeedMeterRepository
import com.example.service.NotificationHelper
import com.example.service.SpeedMeterService

class SpeedMeterApplication : Application() {

    lateinit var repository: SpeedMeterRepository
        private set

    lateinit var preferences: SpeedMeterPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = SpeedMeterPreferences.getInstance(this)
        repository = SpeedMeterRepository.getInstance(this)

        NotificationHelper.createNotificationChannel(this)

        // Automatically start the lightweight foreground monitor if enabled
        if (preferences.isServiceEnabled.value) {
            SpeedMeterService.start(this)
        }
    }
}
