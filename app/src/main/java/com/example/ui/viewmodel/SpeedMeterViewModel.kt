package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SpeedMeterApplication
import com.example.data.model.ColorPalettePreset
import com.example.data.model.DailyUsage
import com.example.data.model.NotificationIconStyle
import com.example.data.model.SpeedUnit
import com.example.data.traffic.LiveSpeedSnapshot
import com.example.service.SpeedMeterService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

enum class HistoryFilter(val label: String) {
    ALL("All Network"),
    WIFI("Wi-Fi Only"),
    MOBILE("Mobile Data")
}

data class PingTestState(
    val isRunning: Boolean = false,
    val latencyMs: Long? = null,
    val status: String = "Ready"
)

class SpeedMeterViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SpeedMeterApplication
    private val repository = app.repository
    private val preferences = app.preferences

    val liveSpeed: StateFlow<LiveSpeedSnapshot> = repository.liveSpeed

    val dailyUsageHistory: StateFlow<List<DailyUsage>> = repository.allDailyUsage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalWifiBytes: StateFlow<Long?> = repository.totalWifiBytes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalMobileBytes: StateFlow<Long?> = repository.totalMobileBytes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // Preference StateFlows
    val isServiceEnabled = preferences.isServiceEnabled
    val updateIntervalMs = preferences.updateIntervalMs
    val pauseWhenScreenOff = preferences.pauseWhenScreenOff
    val hideWhenIdle = preferences.hideWhenIdle
    val batterySaverMode = preferences.batterySaverMode
    val startOnBoot = preferences.startOnBoot
    val speedUnit = preferences.speedUnit
    val iconStyle = preferences.iconStyle
    val palettePreset = preferences.palettePreset
    val showDownloadSpeed = preferences.showDownloadSpeed
    val showUploadSpeed = preferences.showUploadSpeed
    val showDailyUsageInNotification = preferences.showDailyUsageInNotification

    // UI state
    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    private val _selectedUsageDetail = MutableStateFlow<DailyUsage?>(null)
    val selectedUsageDetail: StateFlow<DailyUsage?> = _selectedUsageDetail.asStateFlow()

    private val _pingState = MutableStateFlow(PingTestState())
    val pingState: StateFlow<PingTestState> = _pingState.asStateFlow()

    fun toggleService(enable: Boolean) {
        preferences.setServiceEnabled(enable)
        if (enable) {
            SpeedMeterService.start(getApplication())
        } else {
            SpeedMeterService.stop(getApplication())
        }
    }

    fun setUpdateIntervalMs(interval: Long) {
        preferences.setUpdateIntervalMs(interval)
    }

    fun setPauseWhenScreenOff(pause: Boolean) {
        preferences.setPauseWhenScreenOff(pause)
    }

    fun setHideWhenIdle(hide: Boolean) {
        preferences.setHideWhenIdle(hide)
    }

    fun setBatterySaverMode(enabled: Boolean) {
        preferences.setBatterySaverMode(enabled)
    }

    fun setStartOnBoot(start: Boolean) {
        preferences.setStartOnBoot(start)
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        preferences.setSpeedUnit(unit)
    }

    fun setIconStyle(style: NotificationIconStyle) {
        preferences.setIconStyle(style)
    }

    fun setPalettePreset(preset: ColorPalettePreset) {
        preferences.setPalettePreset(preset)
    }

    fun setShowDownloadSpeed(show: Boolean) {
        preferences.setShowDownloadSpeed(show)
    }

    fun setShowUploadSpeed(show: Boolean) {
        preferences.setShowUploadSpeed(show)
    }

    fun setShowDailyUsageInNotification(show: Boolean) {
        preferences.setShowDailyUsageInNotification(show)
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun selectUsageDetail(usage: DailyUsage?) {
        _selectedUsageDetail.value = usage
    }

    fun deleteUsageDate(date: String) {
        viewModelScope.launch {
            repository.deleteDate(date)
            if (_selectedUsageDetail.value?.date == date) {
                _selectedUsageDetail.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _selectedUsageDetail.value = null
        }
    }

    fun runPingTest() {
        if (_pingState.value.isRunning) return
        _pingState.value = PingTestState(isRunning = true, status = "Pinging 8.8.8.8...")

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val socket = Socket()
                    val startTime = System.currentTimeMillis()
                    socket.connect(InetSocketAddress("8.8.8.8", 53), 2500)
                    val elapsed = System.currentTimeMillis() - startTime
                    socket.close()
                    elapsed
                } catch (e: Exception) {
                    -1L
                }
            }

            if (result >= 0) {
                _pingState.value = PingTestState(
                    isRunning = false,
                    latencyMs = result,
                    status = "Connected ($result ms)"
                )
            } else {
                _pingState.value = PingTestState(
                    isRunning = false,
                    latencyMs = null,
                    status = "Ping timed out"
                )
            }
        }
    }
}
