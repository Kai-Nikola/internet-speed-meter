package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyUsage
import com.example.data.model.SpeedFormatter

@Composable
fun UsageBarChart(
    dailyUsages: List<DailyUsage>,
    wifiColor: Color,
    mobileColor: Color,
    onSelectDay: (DailyUsage) -> Unit,
    modifier: Modifier = Modifier
) {
    // Show up to the last 7 recorded days in chronological order
    val chartDays = remember(dailyUsages) {
        dailyUsages.take(7).reversed()
    }

    var selectedDay by remember(chartDays) {
        mutableStateOf(chartDays.lastOrNull())
    }

    val maxDailyBytes = remember(chartDays) {
        chartDays.maxOfOrNull { it.totalBytes }?.coerceAtLeast(1024 * 1024L) ?: (100 * 1024 * 1024L)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Network Usage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    selectedDay?.let { day ->
                        Text(
                            text = "${SpeedFormatter.formatDateForDisplay(day.date)}: ${SpeedFormatter.formatBytes(day.totalBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = wifiColor
                    ) {}
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Wi-Fi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = mobileColor
                    ) {}
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mobile",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chartDays.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No historical usage data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartDays.forEach { usage ->
                        val isSelected = selectedDay?.date == usage.date
                        val totalBytes = usage.totalBytes
                        val heightFraction = (totalBytes.toFloat() / maxDailyBytes).coerceIn(0.04f, 1f)

                        val wifiPortion = if (totalBytes > 0) (usage.totalWifiBytes.toFloat() / totalBytes) else 1f
                        val mobilePortion = if (totalBytes > 0) (usage.totalMobileBytes.toFloat() / totalBytes) else 0f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedDay = usage
                                    onSelectDay(usage)
                                }
                                .padding(horizontal = 2.dp)
                        ) {
                            // Stacked Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .height((heightFraction * 105).dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    val barWidth = size.width
                                    val barHeight = size.height

                                    val mobileHeight = barHeight * mobilePortion
                                    val wifiHeight = barHeight * wifiPortion

                                    // Draw Wi-Fi bar (bottom/full)
                                    drawRoundRect(
                                        color = if (isSelected) wifiColor else wifiColor.copy(alpha = 0.75f),
                                        topLeft = Offset(0f, 0f),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )

                                    // Draw Mobile bar overlay on top if present
                                    if (mobilePortion > 0.01f) {
                                        drawRoundRect(
                                            color = if (isSelected) mobileColor else mobileColor.copy(alpha = 0.85f),
                                            topLeft = Offset(0f, 0f),
                                            size = Size(barWidth, mobileHeight),
                                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Day Label
                            Text(
                                text = try {
                                    val parts = usage.date.split("-")
                                    "${parts[1]}/${parts[2]}"
                                } catch (_: Exception) {
                                    usage.date.takeLast(5)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
