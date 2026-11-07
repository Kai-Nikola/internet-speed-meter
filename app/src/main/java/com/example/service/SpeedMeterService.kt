package com.example.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.ServiceCompat
import com.example.data.preferences.SpeedMeterPreferences
import com.example.data.repository.SpeedMeterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SpeedMeterService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null

    private lateinit var repository: SpeedMeterRepository
    private lateinit var preferences: SpeedMeterPreferences

    private var isScreenOn = true
    private var idleCounter = 0

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    if (preferences.pauseWhenScreenOff.value) {
                        pauseMonitoring()
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                    resumeMonitoring()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = SpeedMeterRepository.getInstance(applicationContext)
        preferences = SpeedMeterPreferences.getInstance(applicationContext)

        NotificationHelper.createNotificationChannel(applicationContext)

        // Register screen state receiver for low power consumption
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        isScreenOn = powerManager?.isInteractive ?: true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_SERVICE) {
            stopMonitoring()
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceWithNotification()
        startMonitoringLoop()

        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val initialSnapshot = repository.liveSpeed.value
        val notification = NotificationHelper.buildNotification(
            context = this,
            snapshot = initialSnapshot,
            speedUnit = preferences.speedUnit.value,
            iconStyle = preferences.iconStyle.value,
            showDown = preferences.showDownloadSpeed.value,
            showUp = preferences.showUploadSpeed.value,
            showDailyUsage = preferences.showDailyUsageInNotification.value
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            }
            ServiceCompat.startForeground(
                this,
                NotificationHelper.NOTIFICATION_ID,
                notification,
                foregroundType
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoringLoop() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                if (preferences.isServiceEnabled.value) {
                    if (isScreenOn || !preferences.pauseWhenScreenOff.value) {
                        val snapshot = repository.sampleTraffic(isScreenOn = isScreenOn)

                        // Check idle auto-hide feature
                        val totalSpeed = snapshot.downloadBps + snapshot.uploadBps
                        if (preferences.hideWhenIdle.value && totalSpeed <= 1024L) {
                            idleCounter++
                        } else {
                            idleCounter = 0
                        }

                        // Update notification if not suppressed by idle threshold
                        val notification = NotificationHelper.buildNotification(
                            context = this@SpeedMeterService,
                            snapshot = snapshot,
                            speedUnit = preferences.speedUnit.value,
                            iconStyle = preferences.iconStyle.value,
                            showDown = preferences.showDownloadSpeed.value,
                            showUp = preferences.showUploadSpeed.value,
                            showDailyUsage = preferences.showDailyUsageInNotification.value
                        )

                        val notificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notification)
                    }
                }

                val interval = preferences.updateIntervalMs.value.coerceAtLeast(500L)
                delay(interval)
            }
        }
    }

    private fun pauseMonitoring() {
        // Paused to conserve battery when screen is locked
        monitorJob?.cancel()
    }

    private fun resumeMonitoring() {
        startMonitoringLoop()
    }

    private fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}
        stopMonitoring()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_SERVICE = "com.example.action.START_SPEED_METER"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SPEED_METER"

        fun start(context: Context) {
            val intent = Intent(context, SpeedMeterService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SpeedMeterService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
