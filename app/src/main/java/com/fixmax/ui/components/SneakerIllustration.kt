package com.fixmax.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun SneakerIllustration(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sneakerMotion")

    // Gentle floating / running bob motion
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4.5f,
        targetValue = 4.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // Dynamic tilt angle simulating running stride
    val tiltAngle by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tiltAngle"
    )

    // Speed lines pulsation
    val speedLineAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speedLineAlpha"
    )

    val speedLineShift by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speedLineShift"
    )

    Canvas(
        modifier = modifier
            .size(115.dp, 84.dp)
            .graphicsLayer {
                translationY = floatY
                rotationZ = tiltAngle
            }
    ) {
        val w = size.width
        val h = size.height

        // Soft mint motion backdrop oval
        drawOval(
            color = Color(0xFFD6F0E4),
            topLeft = Offset(w * 0.05f, h * 0.25f),
            size = Size(w * 0.9f, h * 0.65f)
        )

        // Animated speed lines with pulsing opacity and subtle shift
        val speedLineColor = Color(0xFFA5DEC5).copy(alpha = speedLineAlpha)
        drawLine(
            speedLineColor,
            Offset(w * 0.05f + speedLineShift, h * 0.74f),
            Offset(w * 0.28f + speedLineShift, h * 0.74f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            speedLineColor,
            Offset(w * 0.02f - speedLineShift, h * 0.84f),
            Offset(w * 0.38f - speedLineShift, h * 0.84f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Sneaker Body (Deep Forest Emerald Green)
        val shoePath = Path().apply {
            moveTo(w * 0.92f, h * 0.65f) // Toe
            cubicTo(w * 0.90f, h * 0.50f, w * 0.70f, h * 0.40f, w * 0.58f, h * 0.42f) // Upper bridge
            cubicTo(w * 0.50f, h * 0.30f, w * 0.38f, h * 0.22f, w * 0.30f, h * 0.28f) // Collar
            cubicTo(w * 0.24f, h * 0.32f, w * 0.22f, h * 0.45f, w * 0.22f, h * 0.62f) // Heel
            cubicTo(w * 0.35f, h * 0.70f, w * 0.70f, h * 0.72f, w * 0.92f, h * 0.65f) // Outsole line
            close()
        }
        drawPath(shoePath, color = Color(0xFF005A43))

        // White Midsole / Sole
        val solePath = Path().apply {
            moveTo(w * 0.20f, h * 0.62f)
            cubicTo(w * 0.40f, h * 0.72f, w * 0.75f, h * 0.72f, w * 0.95f, h * 0.63f)
            lineTo(w * 0.93f, h * 0.72f)
            cubicTo(w * 0.70f, h * 0.80f, w * 0.35f, h * 0.80f, w * 0.18f, h * 0.70f)
            close()
        }
        drawPath(solePath, color = Color.White)

        // Laces / Stripes detail
        val laceColor = Color(0xFF8CE0BE)
        drawLine(laceColor, Offset(w * 0.48f, h * 0.44f), Offset(w * 0.54f, h * 0.52f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(laceColor, Offset(w * 0.55f, h * 0.44f), Offset(w * 0.62f, h * 0.52f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
        drawLine(laceColor, Offset(w * 0.63f, h * 0.45f), Offset(w * 0.70f, h * 0.53f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
    }
}
