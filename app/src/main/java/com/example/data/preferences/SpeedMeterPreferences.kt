package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ColorPalettePreset
import com.example.data.model.NotificationIconStyle
import com.example.data.model.SpeedUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeedMeterPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("speed_meter_settings", Context.MODE_PRIVATE)

    // StateFlows for reactive Compose UI updates
    private val _isServiceEnabled = MutableStateFlow(prefs.getBoolean(KEY_SERVICE_ENABLED, true))
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

    private val _updateIntervalMs = MutableStateFlow(prefs.getLong(KEY_UPDATE_INTERVAL_MS, 1000L))
    val updateIntervalMs: StateFlow<Long> = _updateIntervalMs.asStateFlow()

    private val _pauseWhenScreenOff = MutableStateFlow(prefs.getBoolean(KEY_PAUSE_SCREEN_OFF, true))
    val pauseWhenScreenOff: StateFlow<Boolean> = _pauseWhenScreenOff.asStateFlow()

    private val _hideWhenIdle = MutableStateFlow(prefs.getBoolean(KEY_HIDE_WHEN_IDLE, false))
    val hideWhenIdle: StateFlow<Boolean> = _hideWhenIdle.asStateFlow()

    private val _batterySaverMode = MutableStateFlow(prefs.getBoolean(KEY_BATTERY_SAVER, true))
    val batterySaverMode: StateFlow<Boolean> = _batterySaverMode.asStateFlow()

    private val _startOnBoot = MutableStateFlow(prefs.getBoolean(KEY_START_ON_BOOT, true))
    val startOnBoot: StateFlow<Boolean> = _startOnBoot.asStateFlow()

    private val _speedUnit = MutableStateFlow(
        SpeedUnit.valueOf(prefs.getString(KEY_SPEED_UNIT, SpeedUnit.AUTO.name) ?: SpeedUnit.AUTO.name)
    )
    val speedUnit: StateFlow<SpeedUnit> = _speedUnit.asStateFlow()

    private val _iconStyle = MutableStateFlow(
        NotificationIconStyle.valueOf(
            prefs.getString(KEY_ICON_STYLE, NotificationIconStyle.DYNAMIC_SPEED_TEXT.name)
                ?: NotificationIconStyle.DYNAMIC_SPEED_TEXT.name
        )
    )
    val iconStyle: StateFlow<NotificationIconStyle> = _iconStyle.asStateFlow()

    private val _palettePreset = MutableStateFlow(
        ColorPalettePreset.valueOf(
            prefs.getString(KEY_PALETTE_PRESET, ColorPalettePreset.CYAN_NEON.name)
                ?: ColorPalettePreset.CYAN_NEON.name
        )
    )
    val palettePreset: StateFlow<ColorPalettePreset> = _palettePreset.asStateFlow()

    private val _showDownloadSpeed = MutableStateFlow(prefs.getBoolean(KEY_SHOW_DOWN, true))
    val showDownloadSpeed: StateFlow<Boolean> = _showDownloadSpeed.asStateFlow()

    private val _showUploadSpeed = MutableStateFlow(prefs.getBoolean(KEY_SHOW_UP, true))
    val showUploadSpeed: StateFlow<Boolean> = _showUploadSpeed.asStateFlow()

    private val _showDailyUsageInNotification =
        MutableStateFlow(prefs.getBoolean(KEY_SHOW_DAILY_NOTIFICATION, true))
    val showDailyUsageInNotification: StateFlow<Boolean> = _showDailyUsageInNotification.asStateFlow()

    // Setter methods
    fun setServiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
        _isServiceEnabled.value = enabled
    }

    fun setUpdateIntervalMs(intervalMs: Long) {
        prefs.edit().putLong(KEY_UPDATE_INTERVAL_MS, intervalMs).apply()
        _updateIntervalMs.value = intervalMs
    }

    fun setPauseWhenScreenOff(pause: Boolean) {
        prefs.edit().putBoolean(KEY_PAUSE_SCREEN_OFF, pause).apply()
        _pauseWhenScreenOff.value = pause
    }

    fun setHideWhenIdle(hide: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_WHEN_IDLE, hide).apply()
        _hideWhenIdle.value = hide
    }

    fun setBatterySaverMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BATTERY_SAVER, enabled).apply()
        _batterySaverMode.value = enabled
    }

    fun setStartOnBoot(start: Boolean) {
        prefs.edit().putBoolean(KEY_START_ON_BOOT, start).apply()
        _startOnBoot.value = start
    }

    fun setSpeedUnit(unit: SpeedUnit) {
        prefs.edit().putString(KEY_SPEED_UNIT, unit.name).apply()
        _speedUnit.value = unit
    }

    fun setIconStyle(style: NotificationIconStyle) {
        prefs.edit().putString(KEY_ICON_STYLE, style.name).apply()
        _iconStyle.value = style
    }

    fun setPalettePreset(preset: ColorPalettePreset) {
        prefs.edit().putString(KEY_PALETTE_PRESET, preset.name).apply()
        _palettePreset.value = preset
    }

    fun setShowDownloadSpeed(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_DOWN, show).apply()
        _showDownloadSpeed.value = show
    }

    fun setShowUploadSpeed(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_UP, show).apply()
        _showUploadSpeed.value = show
    }

    fun setShowDailyUsageInNotification(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_DAILY_NOTIFICATION, show).apply()
        _showDailyUsageInNotification.value = show
    }

    companion object {
        private const val KEY_SERVICE_ENABLED = "service_enabled"
        private const val KEY_UPDATE_INTERVAL_MS = "update_interval_ms"
        private const val KEY_PAUSE_SCREEN_OFF = "pause_when_screen_off"
        private const val KEY_HIDE_WHEN_IDLE = "hide_when_idle"
        private const val KEY_BATTERY_SAVER = "battery_saver_mode"
        private const val KEY_START_ON_BOOT = "start_on_boot"
        private const val KEY_SPEED_UNIT = "speed_unit"
        private const val KEY_ICON_STYLE = "icon_style"
        private const val KEY_PALETTE_PRESET = "palette_preset"
        private const val KEY_SHOW_DOWN = "show_download_speed"
        private const val KEY_SHOW_UP = "show_upload_speed"
        private const val KEY_SHOW_DAILY_NOTIFICATION = "show_daily_usage_notification"

        @Volatile
        private var INSTANCE: SpeedMeterPreferences? = null

        fun getInstance(context: Context): SpeedMeterPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = SpeedMeterPreferences(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
