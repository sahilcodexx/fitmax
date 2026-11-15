package com.fixmax.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgressRing(
    progressPercent: Int = 92,
    modifier: Modifier = Modifier
) {
    val animatedPercent by animateFloatAsState(
        targetValue = progressPercent.toFloat().coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progressArcAnim"
    )

    Box(modifier = modifier.size(44.dp)) {
        Canvas(modifier = Modifier.size(44.dp)) {
            val strokeW = 4.5.dp.toPx()
            val radius = (size.minDimension - strokeW) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background Track
            drawCircle(
                color = Color(0xFFD6F0E4),
                radius = radius,
                center = center,
                style = Stroke(width = strokeW)
            )

            // Active Arc
            val sweep = 360f * (animatedPercent / 100f)
            drawArc(
                color = Color(0xFF005A43),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
    }
}
