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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HourlyBarChart(
    data: List<Float> = listOf(
        0.05f, 0.02f, 0.08f, 0.20f, 0.35f, 0.50f, 0.25f, 0.40f, 0.65f, 0.30f,
        0.55f, 0.75f, 0.45f, 0.85f, 0.60f, 1.00f, 0.70f, 0.80f, 0.55f, 0.65f, 0.40f
    ),
    labels: List<String> = listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM"),
    peakIndex: Int = 15,
    peakValueText: String = "1.2k",
    modifier: Modifier = Modifier,
    onBarSelected: ((Int) -> Unit)? = null
) {
    var selectedIndex by remember(data) { mutableStateOf(peakIndex.coerceIn(0, (data.size - 1).coerceAtLeast(0))) }
    val animProgress = remember(data) { Animatable(0f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Dynamic Tooltip glide animation
    val barCount = data.size
    val fraction = if (barCount > 0) (selectedIndex.toFloat() + 0.5f) / barCount else 0.5f
    val targetTooltipOffset = ((fraction - 0.5f) * 220).dp
    val animatedTooltipOffset by animateDpAsState(
        targetValue = targetTooltipOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tooltipSlide"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            if (barCount > 0) {
                                val barSpacing = size.width / barCount
                                val clickedIdx = (offset.x / barSpacing).toInt().coerceIn(0, barCount - 1)
                                selectedIndex = clickedIdx
                                try {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } catch (_: Exception) {}
                                onBarSelected?.invoke(clickedIdx)
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                if (barCount == 0) return@Canvas
                val barSpacing = w / barCount
                val barWidth = (barSpacing * 0.48f).coerceIn(4.dp.toPx(), 18.dp.toPx())

                // Dashed baseline (Material 3 Expressive styling)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                drawLine(
                    color = Color(0xFFD6EAE0),
                    start = Offset(0f, h * 0.65f),
                    end = Offset(w, h * 0.65f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )

                for (i in 0 until barCount) {
                    val x = i * barSpacing + (barSpacing - barWidth) / 2f
                    val animatedVal = data[i] * animProgress.value
                    val barHeight = animatedVal * (h * 0.75f)
                    val y = h - barHeight

                    val isSelected = i == selectedIndex
                    val barColor = if (isSelected) Color(0xFF005A43) else Color(0xFFAFE3CC)

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight.coerceAtLeast(3.dp.toPx())),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Smoothly Sliding Tooltip Badge with Spring Physics
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = animatedTooltipOffset, y = 6.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        // X-axis Time Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = Color(0xFF7A9388),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
