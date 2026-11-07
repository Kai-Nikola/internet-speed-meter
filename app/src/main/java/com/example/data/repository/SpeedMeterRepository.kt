package com.example.data.repository

import android.content.Context
import android.net.TrafficStats
import com.example.data.db.AppDatabase
import com.example.data.db.DailyUsageDao
import com.example.data.model.DailyUsage
import com.example.data.model.SpeedFormatter
import com.example.data.preferences.SpeedMeterPreferences
import com.example.data.traffic.LiveSpeedSnapshot
import com.example.data.traffic.NetworkInfoState
import com.example.data.traffic.NetworkStateHelper
import com.example.data.traffic.NetworkType
import com.example.data.traffic.SpeedPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.LinkedList

class SpeedMeterRepository(
    private val context: Context,
    private val dailyUsageDao: DailyUsageDao,
    private val preferences: SpeedMeterPreferences
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _liveSpeed = MutableStateFlow(LiveSpeedSnapshot())
    val liveSpeed: StateFlow<LiveSpeedSnapshot> = _liveSpeed.asStateFlow()

    val allDailyUsage: Flow<List<DailyUsage>> = dailyUsageDao.getAllDailyUsage()
    val totalWifiBytes: Flow<Long?> = dailyUsageDao.getTotalWifiBytesFlow()
    val totalMobileBytes: Flow<Long?> = dailyUsageDao.getTotalMobileBytesFlow()

    private var lastTotalRxBytes: Long = 0L
    private var lastTotalTxBytes: Long = 0L
    private var lastSampleTimeMs: Long = 0L

    private val speedHistoryQueue = LinkedList<SpeedPoint>()
    private val maxHistoryPoints = 30

    private var todayUsageCache: DailyUsage? = null

    init {
        // Preload today's usage from Room
        repositoryScope.launch {
            val todayStr = SpeedFormatter.getTodayDateString()
            var usage = dailyUsageDao.getUsageForDate(todayStr)
            if (usage == null) {
                usage = DailyUsage(date = todayStr)
                dailyUsageDao.insertOrUpdate(usage)
            }
            todayUsageCache = usage
            _liveSpeed.value = _liveSpeed.value.copy(
                todayWifiBytes = usage.totalWifiBytes,
                todayMobileBytes = usage.totalMobileBytes,
                peakDownloadToday = usage.peakSpeedDown,
                peakUploadToday = usage.peakSpeedUp
            )
        }

        // Initialize TrafficStats baseline
        lastTotalRxBytes = TrafficStats.getTotalRxBytes()
        lastTotalTxBytes = TrafficStats.getTotalTxBytes()
        lastSampleTimeMs = System.currentTimeMillis()
    }

    /**
     * Called periodically by the SpeedMeterService or ViewModel polling loop
     * to compute the latest throughput and accumulate network statistics.
     */
    suspend fun sampleTraffic(isScreenOn: Boolean = true): LiveSpeedSnapshot {
        val currentTimeMs = System.currentTimeMillis()
        val currentRxBytes = TrafficStats.getTotalRxBytes()
        val currentTxBytes = TrafficStats.getTotalTxBytes()

        val timeDeltaMs = currentTimeMs - lastSampleTimeMs
        val timeDeltaSec = if (timeDeltaMs > 0) timeDeltaMs / 1000.0 else 1.0

        var downSpeedBps: Long = 0L
        var upSpeedBps: Long = 0L
        var deltaRx: Long = 0L
        var deltaTx: Long = 0L

        // Handle possible reboots or counter overflows
        if (lastTotalRxBytes > 0 && currentRxBytes >= lastTotalRxBytes && timeDeltaSec > 0) {
            deltaRx = currentRxBytes - lastTotalRxBytes
            downSpeedBps = (deltaRx / timeDeltaSec).toLong()
        }

        if (lastTotalTxBytes > 0 && currentTxBytes >= lastTotalTxBytes && timeDeltaSec > 0) {
            deltaTx = currentTxBytes - lastTotalTxBytes
            upSpeedBps = (deltaTx / timeDeltaSec).toLong()
        }

        // Clamp negative or erroneous values
        if (downSpeedBps < 0) downSpeedBps = 0
        if (upSpeedBps < 0) upSpeedBps = 0

        lastTotalRxBytes = currentRxBytes
        lastTotalTxBytes = currentTxBytes
        lastSampleTimeMs = currentTimeMs

        // Check active network type
        val networkInfo = NetworkStateHelper.getNetworkInfo(context)

        // Today's date check & database sync
        val todayStr = SpeedFormatter.getTodayDateString()
        var currentToday = todayUsageCache
        if (currentToday == null || currentToday.date != todayStr) {
            currentToday = dailyUsageDao.getUsageForDate(todayStr) ?: DailyUsage(date = todayStr)
        }

        var newWifiDown = currentToday.wifiBytesDown
        var newWifiUp = currentToday.wifiBytesUp
        var newMobileDown = currentToday.mobileBytesDown
        var newMobileUp = currentToday.mobileBytesUp
        var newPeakDown = maxOf(currentToday.peakSpeedDown, downSpeedBps)
        var newPeakUp = maxOf(currentToday.peakSpeedUp, upSpeedBps)

        if (networkInfo.type == NetworkType.WIFI) {
            newWifiDown += deltaRx
            newWifiUp += deltaTx
        } else if (networkInfo.type == NetworkType.MOBILE) {
            newMobileDown += deltaRx
            newMobileUp += deltaTx
        } else if (networkInfo.isConnected) {
            // Other transport like Ethernet, categorize with Wi-Fi
            newWifiDown += deltaRx
            newWifiUp += deltaTx
        }

        val updatedUsage = currentToday.copy(
            wifiBytesDown = newWifiDown,
            wifiBytesUp = newWifiUp,
            mobileBytesDown = newMobileDown,
            mobileBytesUp = newMobileUp,
            peakSpeedDown = newPeakDown,
            peakSpeedUp = newPeakUp,
            lastUpdated = currentTimeMs
        )

        todayUsageCache = updatedUsage
        // Persist periodically to Room
        dailyUsageDao.insertOrUpdate(updatedUsage)

        // Update history points for live chart
        synchronized(speedHistoryQueue) {
            speedHistoryQueue.add(SpeedPoint(currentTimeMs, downSpeedBps, upSpeedBps))
            while (speedHistoryQueue.size > maxHistoryPoints) {
                speedHistoryQueue.removeFirst()
            }
        }

        val snapshot = LiveSpeedSnapshot(
            downloadBps = downSpeedBps,
            uploadBps = upSpeedBps,
            peakDownloadToday = newPeakDown,
            peakUploadToday = newPeakUp,
            todayWifiBytes = updatedUsage.totalWifiBytes,
            todayMobileBytes = updatedUsage.totalMobileBytes,
            networkInfo = networkInfo,
            isServiceActive = preferences.isServiceEnabled.value,
            isScreenOn = isScreenOn,
            historySamples = synchronized(speedHistoryQueue) { speedHistoryQueue.toList() }
        )

        _liveSpeed.value = snapshot
        return snapshot
    }

    suspend fun clearHistory() {
        dailyUsageDao.clearAll()
        val todayStr = SpeedFormatter.getTodayDateString()
        val emptyToday = DailyUsage(date = todayStr)
        dailyUsageDao.insertOrUpdate(emptyToday)
        todayUsageCache = emptyToday
        _liveSpeed.value = _liveSpeed.value.copy(
            todayWifiBytes = 0L,
            todayMobileBytes = 0L,
            peakDownloadToday = 0L,
            peakUploadToday = 0L
        )
    }

    suspend fun deleteDate(date: String) {
        dailyUsageDao.deleteDate(date)
    }

    companion object {
        @Volatile
        private var INSTANCE: SpeedMeterRepository? = null

        fun getInstance(context: Context): SpeedMeterRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val prefs = SpeedMeterPreferences.getInstance(context)
                val instance = SpeedMeterRepository(context, db.dailyUsageDao(), prefs)
                INSTANCE = instance
                instance
            }
        }
    }
}
