package com.fixmax.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.ui.theme.*

@Composable
fun ConcentricRippleRing(
    steps: Long,
    goalSteps: Long = 10_000L,
    modifier: Modifier = Modifier
) {
    val progress = (steps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "step_progress"
    )

    // Gentle breathing pulse animation for the organic feel
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) - 24.dp.toPx()

            // Outer Sand Ring
            drawCircle(
                color = RingWarmSand.copy(alpha = 0.5f),
                radius = baseRadius + 22.dp.toPx() * pulseScale,
                center = center,
                style = Stroke(width = 6.dp.toPx())
            )

            // Sky Blue Ring
            drawCircle(
                color = RingSkyBlue.copy(alpha = 0.6f),
                radius = baseRadius + 14.dp.toPx(),
                center = center,
                style = Stroke(width = 8.dp.toPx())
            )

            // Teal Ring
            drawCircle(
                color = RingTeal.copy(alpha = 0.7f),
                radius = baseRadius + 6.dp.toPx() * pulseScale,
                center = center,
                style = Stroke(width = 10.dp.toPx())
            )

            // Inner Soft Mint Track
            drawCircle(
                color = RingGreen,
                radius = baseRadius,
                center = center,
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            // Forest Green Progress Arc
            val sweepAngle = 360f * animatedProgress
            drawArc(
                color = ForestGreenPrimary,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center Content: Minimalist numbers
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "%,d".format(steps),
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = (-1).sp
            )
            Text(
                text = "STEPS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Goal: %,d".format(goalSteps),
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}
