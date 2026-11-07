package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.SpeedMeterPreferences
import com.example.service.SpeedMeterService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val prefs = SpeedMeterPreferences.getInstance(context)
            if (prefs.isServiceEnabled.value && prefs.startOnBoot.value) {
                SpeedMeterService.start(context)
            }
        }
    }
}
