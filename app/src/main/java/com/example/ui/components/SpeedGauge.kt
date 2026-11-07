package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedFormatter
import com.example.data.model.SpeedUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedGauge(
    downloadBps: Long,
    uploadBps: Long,
    speedUnit: SpeedUnit,
    primaryColor: Color,
    downloadColor: Color,
    uploadColor: Color,
    modifier: Modifier = Modifier
) {
    val totalSpeed = downloadBps + uploadBps
    val formattedDown = SpeedFormatter.formatSpeed(downloadBps, speedUnit)
    val formattedUp = SpeedFormatter.formatSpeed(uploadBps, speedUnit)
    val formattedTotal = SpeedFormatter.formatSpeed(totalSpeed, speedUnit)

    // Calculate gauge percentage (logarithmic scale up to 100 MB/s for nice responsiveness)
    val maxRefBps = 100 * 1024 * 1024L // 100 MB/s reference
    val ratio = if (totalSpeed <= 0) 0f else {
        val raw = (totalSpeed.toFloat() / maxRefBps).coerceIn(0f, 1f)
        // Log scale curve for pleasant visual movement across low and high speeds
        (Math.log10(1.0 + 9.0 * raw) / 1.0).toFloat().coerceIn(0f, 1f)
    }

    val animatedSweep by animateFloatAsState(
        targetValue = ratio * 240f,
        animationSpec = tween(durationMillis = 400),
        label = "gaugeSweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
            val arcSize = Size(diameter, diameter)
            val startAngle = 150f
            val totalArc = 240f

            // Background track arc
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = startAngle,
                sweepAngle = totalArc,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Tick marks
            val tickCount = 9
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            for (i in 0 until tickCount) {
                val tickFraction = i.toFloat() / (tickCount - 1)
                val angleDeg = startAngle + tickFraction * totalArc
                val angleRad = angleDeg * (PI / 180f).toFloat()

                val innerR = radius - strokeWidth * 0.9f
                val outerR = radius - strokeWidth * 0.4f

                val startX = center.x + innerR * cos(angleRad)
                val startY = center.y + innerR * sin(angleRad)
                val endX = center.x + outerR * cos(angleRad)
                val endY = center.y + outerR * sin(angleRad)

                val tickActive = tickFraction <= ratio
                drawLine(
                    color = if (tickActive) primaryColor.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.2f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Glowing Active Sweep Arc
            if (animatedSweep > 1f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to downloadColor,
                        0.5f to primaryColor,
                        1.0f to uploadColor,
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Indicator needle bead at head of arc
                val tipAngleDeg = startAngle + animatedSweep
                val tipAngleRad = tipAngleDeg * (PI / 180f).toFloat()
                val tipX = center.x + radius * cos(tipAngleRad)
                val tipY = center.y + radius * sin(tipAngleRad)

                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.45f,
                    center = Offset(tipX, tipY)
                )
            }
        }

        // Center Digital Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TOTAL SPEED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedTotal.value,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formattedTotal.unit,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Sub rates: Down & Up pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Download rate
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Download",
                        tint = downloadColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formattedDown.fullText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Upload rate
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Upload",
                        tint = uploadColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formattedUp.fullText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
