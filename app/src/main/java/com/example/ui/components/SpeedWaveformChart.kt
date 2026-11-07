package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedFormatter
import com.example.data.model.SpeedUnit
import com.example.data.traffic.SpeedPoint

@Composable
fun SpeedWaveformChart(
    samples: List<SpeedPoint>,
    downloadColor: Color,
    uploadColor: Color,
    speedUnit: SpeedUnit,
    modifier: Modifier = Modifier
) {
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
                Text(
                    text = "Live Throughput Stream",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = downloadColor
                    ) {}
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Down",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = uploadColor
                    ) {}
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Up",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Peak rate in window
            val maxDownInWindow = samples.maxOfOrNull { it.downBps } ?: 0L
            val maxUpInWindow = samples.maxOfOrNull { it.upBps } ?: 0L
            val peakInWindow = maxOf(maxDownInWindow, maxUpInWindow, 1024L) // at least 1 KB/s

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val width = size.width
                val height = size.height

                // Draw background horizontal grid lines
                val gridLines = 3
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (samples.size >= 2) {
                    val pointCount = samples.size
                    val stepX = width / (pointCount - 1).coerceAtLeast(1)

                    // Build download path
                    val downPath = Path()
                    val downFillPath = Path()
                    downFillPath.moveTo(0f, height)

                    samples.forEachIndexed { index, point ->
                        val x = index * stepX
                        val normalized = (point.downBps.toFloat() / peakInWindow).coerceIn(0f, 1f)
                        val y = height - (normalized * (height - 10.dp.toPx())) - 5.dp.toPx()

                        if (index == 0) {
                            downPath.moveTo(x, y)
                            downFillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevNorm = (samples[index - 1].downBps.toFloat() / peakInWindow).coerceIn(0f, 1f)
                            val prevY = height - (prevNorm * (height - 10.dp.toPx())) - 5.dp.toPx()
                            val cX = (prevX + x) / 2f
                            downPath.cubicTo(cX, prevY, cX, y, x, y)
                            downFillPath.cubicTo(cX, prevY, cX, y, x, y)
                        }
                    }

                    downFillPath.lineTo(width, height)
                    downFillPath.close()

                    // Draw download fill gradient
                    drawPath(
                        path = downFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                downloadColor.copy(alpha = 0.25f),
                                downloadColor.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Draw download stroke
                    drawPath(
                        path = downPath,
                        color = downloadColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Build upload path
                    val upPath = Path()
                    val upFillPath = Path()
                    upFillPath.moveTo(0f, height)

                    samples.forEachIndexed { index, point ->
                        val x = index * stepX
                        val normalized = (point.upBps.toFloat() / peakInWindow).coerceIn(0f, 1f)
                        val y = height - (normalized * (height - 10.dp.toPx())) - 5.dp.toPx()

                        if (index == 0) {
                            upPath.moveTo(x, y)
                            upFillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevNorm = (samples[index - 1].upBps.toFloat() / peakInWindow).coerceIn(0f, 1f)
                            val prevY = height - (prevNorm * (height - 10.dp.toPx())) - 5.dp.toPx()
                            val cX = (prevX + x) / 2f
                            upPath.cubicTo(cX, prevY, cX, y, x, y)
                            upFillPath.cubicTo(cX, prevY, cX, y, x, y)
                        }
                    }

                    upFillPath.lineTo(width, height)
                    upFillPath.close()

                    // Draw upload fill gradient
                    drawPath(
                        path = upFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                uploadColor.copy(alpha = 0.15f),
                                uploadColor.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Draw upload stroke
                    drawPath(
                        path = upPath,
                        color = uploadColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "30s window",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "Window Peak: ${SpeedFormatter.formatSpeed(peakInWindow, speedUnit).fullText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}
