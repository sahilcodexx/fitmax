package com.fixmax.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BezierSplineChart(
    dataPoints: List<Float> = listOf(0.60f, 0.50f, 0.40f, 0.65f, 0.45f, 0.70f, 0.90f),
    labels: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    peakValueText: String = "7.8k",
    modifier: Modifier = Modifier,
    onPointSelected: ((Int) -> Unit)? = null
) {
    var selectedIndex by remember(dataPoints) { mutableStateOf(dataPoints.size - 1) }
    val animProgress = remember(dataPoints) { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splineGlow")
    val dotPulseRadius by infiniteTransition.animateFloat(
        initialValue = 4.dp.value,
        targetValue = 7.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulseRadius"
    )

    // Animated Tooltip Position
    val n = dataPoints.size
    val fraction = if (n > 1) selectedIndex.toFloat() / (n - 1) else 1f
    val targetTooltipOffset = ((fraction - 0.5f) * 230).dp
    val animatedTooltipOffset by animateDpAsState(
        targetValue = targetTooltipOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "splineTooltipSlide"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            if (n > 1) {
                                val stepX = size.width / (n - 1)
                                val clickedIdx = ((offset.x + (stepX / 2f)) / stepX).toInt().coerceIn(0, n - 1)
                                selectedIndex = clickedIdx
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } catch (_: Exception) {}
                                onPointSelected?.invoke(clickedIdx)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                if (n < 2) return@Canvas
                val stepX = w / (n - 1)

                val points = dataPoints.mapIndexed { i, v ->
                    val animatedY = v * animProgress.value
                    Offset(i * stepX, h - (animatedY * (h * 0.75f)).coerceAtLeast(10.dp.toPx()))
                }

                // Cubic Bezier Spline Path
                val curvePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p0 = if (i > 0) points[i - 1] else points[i]
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val p3 = if (i + 2 < points.size) points[i + 2] else p2

                        val c1x = p1.x + (p2.x - p0.x) / 6f
                        val c1y = p1.y + (p2.y - p0.y) / 6f
                        val c2x = p2.x - (p3.x - p1.x) / 6f
                        val c2y = p2.y - (p3.y - p1.y) / 6f

                        cubicTo(c1x, c1y, c2x, c2y, p2.x, p2.y)
                    }
                }

                // Gradient Fill Path
                val fillPath = Path().apply {
                    addPath(curvePath)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF5BAA88).copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f,
                        endY = h
                    )
                )

                // Spline Line
                drawPath(
                    path = curvePath,
                    color = Color(0xFF005A43),
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Anchor Dot with breathing pulse for selected point
                val targetPoint = points[selectedIndex.coerceIn(0, points.size - 1)]
                drawCircle(
                    color = Color(0xFF5BAA88).copy(alpha = 0.3f),
                    radius = (dotPulseRadius + 4).dp.toPx(),
                    center = targetPoint
                )
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = targetPoint)
                drawCircle(color = Color(0xFF005A43), radius = 4.dp.toPx(), center = targetPoint)
            }

            // Sliding Tooltip positioned relative to selected point
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = animatedTooltipOffset, y = 8.dp)
                    .background(Color(0xFF005A43), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = peakValueText,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Weekday Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { day ->
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = Color(0xFF7A9388),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
