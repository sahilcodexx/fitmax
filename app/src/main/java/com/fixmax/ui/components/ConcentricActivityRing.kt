package com.fixmax.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Concentric 3-Layer Activity Circle Bar (Steps, Active Time, Calories).
 * Outer Ring: Vibrant Green (Steps)
 * Middle Ring: Sky Cyan / Blue (Active Time)
 * Inner Ring: Vivid Pink / Magenta (Calories)
 */
@Composable
fun ConcentricActivityRing(
    stepsProgress: Float = 0.78f,
    activeTimeProgress: Float = 0.90f,
    caloriesProgress: Float = 0.64f,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    ringSpacing: Dp = 3.dp
) {
    val animSteps = remember { Animatable(0f) }
    val animActive = remember { Animatable(0f) }
    val animCalories = remember { Animatable(0f) }

    LaunchedEffect(stepsProgress) {
        animSteps.snapTo(0f)
        animSteps.animateTo(
            targetValue = stepsProgress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(activeTimeProgress) {
        animActive.snapTo(0f)
        animActive.animateTo(
            targetValue = activeTimeProgress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1150, delayMillis = 100, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(caloriesProgress) {
        animCalories.snapTo(0f)
        animCalories.animateTo(
            targetValue = caloriesProgress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1300, delayMillis = 200, easing = FastOutSlowInEasing)
        )
    }

    // Color Palette
    val greenActive = Color(0xFF2ECC71)
    val greenTrack = Color(0xFFE2F7EB)

    val blueActive = Color(0xFF00B4D8)
    val blueTrack = Color(0xFFE0F7FA)

    val pinkActive = Color(0xFFFF2D75)
    val pinkTrack = Color(0xFFFFE3ED)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sWidth = strokeWidth.toPx()
            val spacing = ringSpacing.toPx()
            val totalRadius = this.size.minDimension / 2f - (sWidth / 2f)

            val rings = listOf(
                Triple(totalRadius, animSteps.value to (greenActive to greenTrack), sWidth),
                Triple(totalRadius - sWidth - spacing, animActive.value to (blueActive to blueTrack), sWidth),
                Triple(totalRadius - (sWidth + spacing) * 2f, animCalories.value to (pinkActive to pinkTrack), sWidth)
            )

            rings.forEach { (radius, data, width) ->
                if (radius > width / 2f) {
                    val (progress, colors) = data
                    val (activeColor, trackColor) = colors
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2f, radius * 2f)

                    // 1. Background full track
                    drawCircle(
                        color = trackColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = width)
                    )

                    // 2. Animated active arc
                    if (progress > 0.005f) {
                        drawArc(
                            color = activeColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = width, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
