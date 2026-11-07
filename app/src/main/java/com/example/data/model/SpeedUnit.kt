package com.example.data.model

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SpeedUnit(val displayName: String, val suffix: String) {
    AUTO("Auto (KB/s, MB/s)", ""),
    KILOBYTES("Kilobytes (KB/s)", "KB/s"),
    MEGABYTES("Megabytes (MB/s)", "MB/s"),
    MEGABITS("Megabits (Mbps)", "Mbps")
}

enum class ColorPalettePreset(
    val title: String,
    val primaryColorHex: Long,
    val downloadColorHex: Long,
    val uploadColorHex: Long,
    val accentGlowHex: Long
) {
    CYAN_NEON("Cyber Cyan", 0xFF00E5FF, 0xFF00E676, 0xFF00E5FF, 0x3300E5FF),
    EMERALD_MINT("Emerald Mint", 0xFF00E676, 0xFF00E676, 0xFFFFAB00, 0x3300E676),
    ELECTRIC_VIOLET("Electric Violet", 0xFFA855F7, 0xFF38BDF8, 0xFFA855F7, 0x33A855F7),
    SUNSET_AMBER("Sunset Amber", 0xFFFF9100, 0xFF00E5FF, 0xFFFF9100, 0x33FF9100),
    CRIMSON_FLAME("Neon Rose", 0xFFFF2A6D, 0xFF05D9E8, 0xFFFF2A6D, 0x33FF2A6D)
}

enum class NotificationIconStyle(val title: String, val description: String) {
    DYNAMIC_SPEED_TEXT("Live Speed Number", "Shows live rate numbers in the status bar (e.g. 1.2M, 450K)"),
    SPEEDOMETER_ICON("Speedometer Icon", "Lightweight vector speedometer icon"),
    MINIMAL_DOT("Minimal Glyph", "Ultra clean minimal network dot indicator")
}

data class SpeedFormatted(
    val value: String,
    val unit: String
) {
    val fullText: String get() = "$value $unit"
}

object SpeedFormatter {
    private val decimalFormatOne = DecimalFormat("#,##0.0")
    private val decimalFormatZero = DecimalFormat("#,##0")

    fun formatSpeed(bytesPerSec: Long, unitSetting: SpeedUnit = SpeedUnit.AUTO): SpeedFormatted {
        if (bytesPerSec <= 0) {
            return when (unitSetting) {
                SpeedUnit.MEGABITS -> SpeedFormatted("0.0", "Mbps")
                SpeedUnit.MEGABYTES -> SpeedFormatted("0.0", "MB/s")
                SpeedUnit.KILOBYTES -> SpeedFormatted("0", "KB/s")
                SpeedUnit.AUTO -> SpeedFormatted("0", "KB/s")
            }
        }

        return when (unitSetting) {
            SpeedUnit.KILOBYTES -> {
                val kb = bytesPerSec / 1024.0
                SpeedFormatted(if (kb >= 100) decimalFormatZero.format(kb) else decimalFormatOne.format(kb), "KB/s")
            }
            SpeedUnit.MEGABYTES -> {
                val mb = bytesPerSec / (1024.0 * 1024.0)
                SpeedFormatted(decimalFormatOne.format(mb), "MB/s")
            }
            SpeedUnit.MEGABITS -> {
                val mbps = (bytesPerSec * 8.0) / (1000.0 * 1000.0)
                SpeedFormatted(decimalFormatOne.format(mbps), "Mbps")
            }
            SpeedUnit.AUTO -> {
                when {
                    bytesPerSec >= 1024 * 1024 * 10 -> {
                        // >= 10 MB/s
                        val mb = bytesPerSec / (1024.0 * 1024.0)
                        SpeedFormatted(decimalFormatZero.format(mb), "MB/s")
                    }
                    bytesPerSec >= 1024 * 1024 -> {
                        // 1 to 10 MB/s
                        val mb = bytesPerSec / (1024.0 * 1024.0)
                        SpeedFormatted(decimalFormatOne.format(mb), "MB/s")
                    }
                    bytesPerSec >= 1024 -> {
                        val kb = bytesPerSec / 1024.0
                        SpeedFormatted(if (kb >= 100) decimalFormatZero.format(kb) else decimalFormatOne.format(kb), "KB/s")
                    }
                    else -> {
                        SpeedFormatted(bytesPerSec.toString(), "B/s")
                    }
                }
            }
        }
    }

    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        if (kb < 1.0) return "$bytes B"
        val mb = kb / 1024.0
        if (mb < 1.0) return "${decimalFormatOne.format(kb)} KB"
        val gb = mb / 1024.0
        if (gb < 1.0) return "${decimalFormatOne.format(mb)} MB"
        val tb = gb / 1024.0
        if (tb < 1.0) return "${decimalFormatOne.format(gb)} GB"
        return "${decimalFormatOne.format(tb)} TB"
    }

    fun formatCompactSpeedForNotification(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 K"
        val kb = bytesPerSec / 1024.0
        return if (kb >= 1000.0) {
            val mb = kb / 1024.0
            "${decimalFormatOne.format(mb)}M"
        } else {
            "${kb.toInt()}K"
        }
    }

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateForDisplay(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateStr) ?: return dateStr
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            if (dateStr == today) return "Today"
            
            // Check yesterday
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            if (dateStr == yesterday) return "Yesterday"

            val outputFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }
}
