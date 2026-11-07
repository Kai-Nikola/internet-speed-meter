package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.SpeedMeterService
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SpeedMeterTheme
import com.example.ui.viewmodel.SpeedMeterViewModel

enum class NavigationDestination(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
) {
    HOME("Dashboard", Icons.Filled.Speed, Icons.Outlined.Speed),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SpeedMeterViewModel = viewModel()
            val palettePreset by viewModel.palettePreset.collectAsStateWithLifecycle()

            SpeedMeterTheme(
                darkTheme = true,
                palette = palettePreset
            ) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: SpeedMeterViewModel) {
    val context = LocalContext.current
    var currentDestination by rememberSaveable { mutableStateOf(NavigationDestination.HOME) }

    // Collect UI States
    val liveSpeed by viewModel.liveSpeed.collectAsStateWithLifecycle()
    val dailyUsages by viewModel.dailyUsageHistory.collectAsStateWithLifecycle()
    val totalWifiBytes by viewModel.totalWifiBytes.collectAsStateWithLifecycle()
    val totalMobileBytes by viewModel.totalMobileBytes.collectAsStateWithLifecycle()
    val isServiceEnabled by viewModel.isServiceEnabled.collectAsStateWithLifecycle()
    val updateIntervalMs by viewModel.updateIntervalMs.collectAsStateWithLifecycle()
    val pauseWhenScreenOff by viewModel.pauseWhenScreenOff.collectAsStateWithLifecycle()
    val hideWhenIdle by viewModel.hideWhenIdle.collectAsStateWithLifecycle()
    val batterySaverMode by viewModel.batterySaverMode.collectAsStateWithLifecycle()
    val startOnBoot by viewModel.startOnBoot.collectAsStateWithLifecycle()
    val speedUnit by viewModel.speedUnit.collectAsStateWithLifecycle()
    val iconStyle by viewModel.iconStyle.collectAsStateWithLifecycle()
    val palettePreset by viewModel.palettePreset.collectAsStateWithLifecycle()
    val showDownloadSpeed by viewModel.showDownloadSpeed.collectAsStateWithLifecycle()
    val showUploadSpeed by viewModel.showUploadSpeed.collectAsStateWithLifecycle()
    val showDailyUsageInNotification by viewModel.showDailyUsageInNotification.collectAsStateWithLifecycle()
    val historyFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val pingState by viewModel.pingState.collectAsStateWithLifecycle()

    val primaryColor = Color(palettePreset.primaryColorHex)

    // Request notification permission on Android 13+ (TIRAMISU)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && isServiceEnabled) {
            SpeedMeterService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination) {
                            NavigationDestination.HOME -> "Internet Speed Meter"
                            NavigationDestination.HISTORY -> "Network Usage History"
                            NavigationDestination.SETTINGS -> "Settings & Power"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = primaryColor,
                            indicatorColor = primaryColor,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavigationDestination.HOME -> {
                    HomeScreen(
                        liveSpeed = liveSpeed,
                        isServiceEnabled = isServiceEnabled,
                        speedUnit = speedUnit,
                        palettePreset = palettePreset,
                        pauseWhenScreenOff = pauseWhenScreenOff,
                        pingState = pingState,
                        onToggleService = { viewModel.toggleService(it) },
                        onRunPingTest = { viewModel.runPingTest() }
                    )
                }
                NavigationDestination.HISTORY -> {
                    HistoryScreen(
                        dailyUsages = dailyUsages,
                        totalWifiBytes = totalWifiBytes ?: 0L,
                        totalMobileBytes = totalMobileBytes ?: 0L,
                        filter = historyFilter,
                        speedUnit = speedUnit,
                        palettePreset = palettePreset,
                        onFilterChange = { viewModel.setHistoryFilter(it) },
                        onDeleteDate = { viewModel.deleteUsageDate(it) },
                        onClearAll = { viewModel.clearAllHistory() }
                    )
                }
                NavigationDestination.SETTINGS -> {
                    SettingsScreen(
                        updateIntervalMs = updateIntervalMs,
                        pauseWhenScreenOff = pauseWhenScreenOff,
                        hideWhenIdle = hideWhenIdle,
                        batterySaverMode = batterySaverMode,
                        startOnBoot = startOnBoot,
                        speedUnit = speedUnit,
                        iconStyle = iconStyle,
                        palettePreset = palettePreset,
                        showDownloadSpeed = showDownloadSpeed,
                        showUploadSpeed = showUploadSpeed,
                        showDailyUsageInNotification = showDailyUsageInNotification,
                        onSetUpdateInterval = { viewModel.setUpdateIntervalMs(it) },
                        onSetPauseWhenScreenOff = { viewModel.setPauseWhenScreenOff(it) },
                        onSetHideWhenIdle = { viewModel.setHideWhenIdle(it) },
                        onSetBatterySaverMode = { viewModel.setBatterySaverMode(it) },
                        onSetStartOnBoot = { viewModel.setStartOnBoot(it) },
                        onSetSpeedUnit = { viewModel.setSpeedUnit(it) },
                        onSetIconStyle = { viewModel.setIconStyle(it) },
                        onSetPalettePreset = { viewModel.setPalettePreset(it) },
                        onSetShowDownloadSpeed = { viewModel.setShowDownloadSpeed(it) },
                        onSetShowUploadSpeed = { viewModel.setShowUploadSpeed(it) },
                        onSetShowDailyUsageInNotification = { viewModel.setShowDailyUsageInNotification(it) }
                    )
                }
            }
        }
    }
}
