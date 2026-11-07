package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorPalettePreset
import com.example.data.model.DailyUsage
import com.example.data.model.SpeedFormatter
import com.example.data.model.SpeedUnit
import com.example.ui.components.UsageBarChart
import com.example.ui.viewmodel.HistoryFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    dailyUsages: List<DailyUsage>,
    totalWifiBytes: Long,
    totalMobileBytes: Long,
    filter: HistoryFilter,
    speedUnit: SpeedUnit,
    palettePreset: ColorPalettePreset,
    onFilterChange: (HistoryFilter) -> Unit,
    onDeleteDate: (String) -> Unit,
    onClearAll: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val primaryColor = Color(palettePreset.primaryColorHex)
    val wifiColor = Color(palettePreset.downloadColorHex)
    val mobileColor = Color(palettePreset.uploadColorHex)

    var showClearDialog by remember { mutableStateOf(false) }
    var selectedDayDetail by remember { mutableStateOf<DailyUsage?>(null) }
    var dayToDelete by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(dailyUsages, filter) {
        when (filter) {
            HistoryFilter.ALL -> dailyUsages
            HistoryFilter.WIFI -> dailyUsages.filter { it.totalWifiBytes > 0 }
            HistoryFilter.MOBILE -> dailyUsages.filter { it.totalMobileBytes > 0 }
        }
    }

    val grandTotalBytes = totalWifiBytes + totalMobileBytes

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Overview Cards
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Wi-Fi
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Total Wi-Fi",
                                tint = wifiColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All-Time Wi-Fi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = SpeedFormatter.formatBytes(totalWifiBytes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Total Mobile
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = "Total Mobile",
                                tint = mobileColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "All-Time Mobile",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = SpeedFormatter.formatBytes(totalMobileBytes),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Daily Usage Visual Bar Chart
        item {
            UsageBarChart(
                dailyUsages = dailyUsages,
                wifiColor = wifiColor,
                mobileColor = mobileColor,
                onSelectDay = { selectedDayDetail = it }
            )
        }

        // Filter Chips & Clear Action Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    HistoryFilter.values().forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { onFilterChange(option) },
                            label = {
                                Text(
                                    text = option.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (filter == option) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                                selectedLabelColor = primaryColor
                            )
                        )
                    }
                }

                if (dailyUsages.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Empty state or daily records list
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "No data",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No usage records logged yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Daily statistics accumulate automatically as you browse",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.date }) { usage ->
                DailyUsageItemCard(
                    usage = usage,
                    filter = filter,
                    wifiColor = wifiColor,
                    mobileColor = mobileColor,
                    primaryColor = primaryColor,
                    speedUnit = speedUnit,
                    onDelete = { dayToDelete = usage.date },
                    onClick = { selectedDayDetail = usage }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Clear All Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Usage History?") },
            text = { Text("This will permanently delete all daily network statistics and reset counters.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Single Day Delete Dialog
    if (dayToDelete != null) {
        val dateTarget = dayToDelete!!
        AlertDialog(
            onDismissRequest = { dayToDelete = null },
            title = { Text("Delete Log for $dateTarget?") },
            text = { Text("Remove the network usage record for this specific day.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDate(dateTarget)
                        dayToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dayToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DailyUsageItemCard(
    usage: DailyUsage,
    filter: HistoryFilter,
    wifiColor: Color,
    mobileColor: Color,
    primaryColor: Color,
    speedUnit: SpeedUnit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = SpeedFormatter.formatDateForDisplay(usage.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = usage.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = SpeedFormatter.formatBytes(
                            when (filter) {
                                HistoryFilter.ALL -> usage.totalBytes
                                HistoryFilter.WIFI -> usage.totalWifiBytes
                                HistoryFilter.MOBILE -> usage.totalMobileBytes
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Day Record",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub Breakdown Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Wi-Fi breakdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = wifiColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Wi-Fi: ${SpeedFormatter.formatBytes(usage.totalWifiBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Mobile breakdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "Mobile Data",
                        tint = mobileColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mobile: ${SpeedFormatter.formatBytes(usage.totalMobileBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (usage.peakSpeedDown > 0 || usage.peakSpeedUp > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Peak: ↓ ${SpeedFormatter.formatSpeed(usage.peakSpeedDown, speedUnit).fullText}  ↑ ${SpeedFormatter.formatSpeed(usage.peakSpeedUp, speedUnit).fullText}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
