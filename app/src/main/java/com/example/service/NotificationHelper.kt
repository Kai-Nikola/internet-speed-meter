package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import androidx.core.graphics.drawable.IconCompat
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.NotificationIconStyle
import com.example.data.model.SpeedFormatter
import com.example.data.model.SpeedUnit
import com.example.data.traffic.LiveSpeedSnapshot
import com.example.data.traffic.NetworkType

object NotificationHelper {
    const val CHANNEL_ID = "speed_meter_live_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Real-time Speed Meter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays live internet upload and download speed in the status bar"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        context: Context,
        snapshot: LiveSpeedSnapshot,
        speedUnit: SpeedUnit,
        iconStyle: NotificationIconStyle,
        showDown: Boolean,
        showUp: Boolean,
        showDailyUsage: Boolean
    ): Notification {
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val downFormatted = SpeedFormatter.formatSpeed(snapshot.downloadBps, speedUnit)
        val upFormatted = SpeedFormatter.formatSpeed(snapshot.uploadBps, speedUnit)

        val title = when {
            showDown && showUp -> "↓ ${downFormatted.fullText}   ↑ ${upFormatted.fullText}"
            showDown -> "↓ ${downFormatted.fullText}"
            showUp -> "↑ ${upFormatted.fullText}"
            else -> "↓ ${downFormatted.fullText}"
        }

        val netLabel = snapshot.networkInfo.detail
        val todayWifi = SpeedFormatter.formatBytes(snapshot.todayWifiBytes)
        val todayMobile = SpeedFormatter.formatBytes(snapshot.todayMobileBytes)
        val todayTotal = SpeedFormatter.formatBytes(snapshot.totalTodayBytes)

        val contentText = if (showDailyUsage) {
            "Today: $todayTotal  |  Wi-Fi: $todayWifi  •  Mobile: $todayMobile"
        } else {
            "Network: $netLabel"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSubText(snapshot.networkInfo.type.label)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)

        // Set small status bar icon based on user preference
        when (iconStyle) {
            NotificationIconStyle.DYNAMIC_SPEED_TEXT -> {
                val totalSpeed = snapshot.downloadBps + snapshot.uploadBps
                val bitmap = createSpeedBitmap(totalSpeed)
                builder.setSmallIcon(IconCompat.createWithBitmap(bitmap))
            }
            NotificationIconStyle.SPEEDOMETER_ICON,
            NotificationIconStyle.MINIMAL_DOT -> {
                builder.setSmallIcon(R.drawable.ic_stat_speed)
            }
        }

        return builder.build()
    }

    /**
     * Dynamically generates a high-contrast, ultra-sharp bitmap showing the current speed
     * directly inside the Android status bar icon slot!
     */
    private fun createSpeedBitmap(speedBps: Long): Bitmap {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paintNumber = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val paintUnit = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        var numText: String
        var unitText: String

        val kb = speedBps / 1024.0
        when {
            speedBps <= 0 -> {
                numText = "0"
                unitText = "KB"
            }
            kb >= 1000.0 -> {
                val mb = kb / 1024.0
                numText = if (mb >= 10.0) "${mb.toInt()}" else String.format("%.1f", mb)
                unitText = "MB"
            }
            kb >= 100.0 -> {
                numText = "${kb.toInt()}"
                unitText = "KB"
            }
            else -> {
                numText = String.format("%.1f", kb)
                unitText = "KB"
            }
        }

        if (numText.length >= 4) {
            paintNumber.textSize = 42f
        } else if (numText.length == 3) {
            paintNumber.textSize = 48f
        } else {
            paintNumber.textSize = 54f
        }

        paintUnit.textSize = 28f

        // Draw Speed Number on top half and Unit on bottom half
        canvas.drawText(numText, size / 2f, 48f, paintNumber)
        canvas.drawText(unitText, size / 2f, 84f, paintUnit)

        return bitmap
    }
}
