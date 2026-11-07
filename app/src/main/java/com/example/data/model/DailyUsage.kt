package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing network data usage statistics for a single day.
 * Date format is "yyyy-MM-dd" (e.g., "2026-08-29").
 */
@Entity(tableName = "daily_usage")
data class DailyUsage(
    @PrimaryKey
    val date: String,
    val wifiBytesDown: Long = 0L,
    val wifiBytesUp: Long = 0L,
    val mobileBytesDown: Long = 0L,
    val mobileBytesUp: Long = 0L,
    val peakSpeedDown: Long = 0L, // in Bytes per second
    val peakSpeedUp: Long = 0L,   // in Bytes per second
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalWifiBytes: Long get() = wifiBytesDown + wifiBytesUp
    val totalMobileBytes: Long get() = mobileBytesDown + mobileBytesUp
    val totalBytes: Long get() = totalWifiBytes + totalMobileBytes
    val totalDownBytes: Long get() = wifiBytesDown + mobileBytesDown
    val totalUpBytes: Long get() = wifiBytesUp + mobileBytesUp
}
