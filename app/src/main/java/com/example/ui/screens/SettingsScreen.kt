package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorPalettePreset
import com.example.data.model.NotificationIconStyle
import com.example.data.model.SpeedUnit

@Composable
fun SettingsScreen(
    updateIntervalMs: Long,
    pauseWhenScreenOff: Boolean,
    hideWhenIdle: Boolean,
    batterySaverMode: Boolean,
    startOnBoot: Boolean,
    speedUnit: SpeedUnit,
    iconStyle: NotificationIconStyle,
    palettePreset: ColorPalettePreset,
    showDownloadSpeed: Boolean,
    showUploadSpeed: Boolean,
    showDailyUsageInNotification: Boolean,
    onSetUpdateInterval: (Long) -> Unit,
    onSetPauseWhenScreenOff: (Boolean) -> Unit,
    onSetHideWhenIdle: (Boolean) -> Unit,
    onSetBatterySaverMode: (Boolean) -> Unit,
    onSetStartOnBoot: (Boolean) -> Unit,
    onSetSpeedUnit: (SpeedUnit) -> Unit,
    onSetIconStyle: (NotificationIconStyle) -> Unit,
    onSetPalettePreset: (ColorPalettePreset) -> Unit,
    onSetShowDownloadSpeed: (Boolean) -> Unit,
    onSetShowUploadSpeed: (Boolean) -> Unit,
    onSetShowDailyUsageInNotification: (Boolean) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val primaryColor = Color(palettePreset.primaryColorHex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Low Power Consumption Settings
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(
                title = "Low Power Consumption",
                subtitle = "Battery life optimization features",
                icon = Icons.Default.BatterySaver,
                tint = primaryColor
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Update Interval Picker
                    Text(
                        text = "Refresh Rate / Update Interval",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Longer interval drastically minimizes CPU activity and extends battery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val intervals = listOf(
                        Triple(1000L, "1.0s", "Standard"),
                        Triple(2000L, "2.0s", "Balanced"),
                        Triple(3000L, "3.0s", "Eco Saver"),
                        Triple(5000L, "5.0s", "Max Battery")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervals.forEach { (ms, label, desc) ->
                            val isSelected = updateIntervalMs == ms
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) primaryColor else Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSetUpdateInterval(ms) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pause when screen off
                    SettingToggleRow(
                        title = "Auto-Pause on Screen Off",
                        description = "Completely pauses sampling when screen is locked (0% CPU wakeups)",
                        checked = pauseWhenScreenOff,
                        onCheckedChange = onSetPauseWhenScreenOff,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hide when idle
                    SettingToggleRow(
                        title = "Auto-Hide when Idle",
                        description = "Reduces updates when speed is below 1 KB/s to save energy",
                        checked = hideWhenIdle,
                        onCheckedChange = onSetHideWhenIdle,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Start on boot
                    SettingToggleRow(
                        title = "Start Automatically on Boot",
                        description = "Resumes speed meter background monitoring when phone restarts",
                        checked = startOnBoot,
                        onCheckedChange = onSetStartOnBoot,
                        primaryColor = primaryColor
                    )
                }
            }
        }

        // Section 2: Custom Colors & Themes
        item {
            SectionHeader(
                title = "Custom Colors & Theme",
                subtitle = "Personalize dial & rate indicators",
                icon = Icons.Default.ColorLens,
                tint = primaryColor
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Color Palette Presets",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ColorPalettePreset.values().forEach { preset ->
                        val isSelected = palettePreset == preset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSetPalettePreset(preset) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Swatch circles
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = CircleShape,
                                    color = Color(preset.primaryColorHex)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = CircleShape,
                                    color = Color(preset.downloadColorHex)
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = CircleShape,
                                    color = Color(preset.uploadColorHex)
                                ) {}

                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            RadioButton(
                                selected = isSelected,
                                onClick = { onSetPalettePreset(preset) },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Lightweight Notification & Status Bar Design
        item {
            SectionHeader(
                title = "Status Bar & Notification Design",
                subtitle = "Custom notification styles & display formats",
                icon = Icons.Default.Notifications,
                tint = primaryColor
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status Bar Icon Style",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    NotificationIconStyle.values().forEach { style ->
                        val isSelected = iconStyle == style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetIconStyle(style) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = style.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSetIconStyle(style) },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed Unit Selector
                    Text(
                        text = "Speed Unit Display",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SpeedUnit.values().forEach { unit ->
                        val isSelected = speedUnit == unit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSetSpeedUnit(unit) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = unit.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSetSpeedUnit(unit) },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingToggleRow(
                        title = "Show Download Speed",
                        description = "Display active download rate in notification",
                        checked = showDownloadSpeed,
                        onCheckedChange = onSetShowDownloadSpeed,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Show Upload Speed",
                        description = "Display active upload rate in notification",
                        checked = showUploadSpeed,
                        onCheckedChange = onSetShowUploadSpeed,
                        primaryColor = primaryColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Show Daily Data Summary",
                        description = "Include today's total Wi-Fi & mobile usage in notification text",
                        checked = showDailyUsageInNotification,
                        onCheckedChange = onSetShowDailyUsageInNotification,
                        primaryColor = primaryColor
                    )
                }
            }
        }

        // Section 4: About & Architecture
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Speed Meter v1.0",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lightweight status bar speed meter built with Room persistence, Kotlin Coroutines, and power-saving architecture.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = tint.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = primaryColor
            )
        )
    }
}
