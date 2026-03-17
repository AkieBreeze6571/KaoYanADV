package com.example.kaoyanadventure.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun LevelRing(
    level: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    ringSize: Dp = 64.dp,
    strokeWidth: Dp = 8.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    expText: String? = null
) {
    val p = progress.coerceIn(0f, 1f)
    val isFull = p >= 0.999f
    val pulseScale = if (isFull) {
        val transition = rememberInfiniteTransition(label = "lvlPulse")
        val animated by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 950),
                repeatMode = RepeatMode.Reverse
            ),
            label = "lvlPulseScale"
        )
        animated
    } else {
        1f
    }

    val titleStyle = MaterialTheme.typography.labelSmall
    val levelStyle = if (ringSize < 72.dp) {
        MaterialTheme.typography.titleMedium
    } else {
        MaterialTheme.typography.titleLarge
    }
    val subStyle = MaterialTheme.typography.labelSmall

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(ringSize)
                .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            ) {
                val strokePx = strokeWidth.toPx()
                val diameter = min(this.size.width, this.size.height) - strokePx
                if (diameter <= 0f) return@Canvas

                val topLeft = Offset(
                    (this.size.width - diameter) / 2f,
                    (this.size.height - diameter) / 2f
                )
                val arcSize = Size(diameter, diameter)

                val startAngle = 135f
                val sweepAngle = 270f

                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                drawArc(
                    color = progressColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * p,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = diameter / 2f
                val tickOuter = radius - strokePx * 0.12f
                val tickInner = tickOuter - strokePx * 0.55f
                val tickColor = textColor.copy(alpha = 0.75f)

                // 10 个刻度：每 10% 一个，沿仪表盘弧分布。
                for (i in 1..10) {
                    val t = i / 10f
                    val angleDeg = startAngle + sweepAngle * t
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val cosA = cos(angleRad).toFloat()
                    val sinA = sin(angleRad).toFloat()

                    val p1 = Offset(center.x + tickInner * cosA, center.y + tickInner * sinA)
                    val p2 = Offset(center.x + tickOuter * cosA, center.y + tickOuter * sinA)

                    drawLine(
                        color = tickColor,
                        start = p1,
                        end = p2,
                        strokeWidth = (strokePx * 0.18f).coerceAtLeast(1f),
                        cap = StrokeCap.Round
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "RANK",
                    color = textColor.copy(alpha = 0.85f),
                    style = titleStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "$level",
                    color = textColor,
                    style = levelStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = expText?.takeIf { it.isNotBlank() } ?: "NEXT",
                    color = textColor.copy(alpha = 0.9f),
                    style = subStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
