package com.fixmax.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/**
 * 1. Samsung Health Orange Active Workout Hero Card.
 * Matches Image 1 (Orange Card with 7.96 km, Walking, Timer, Steps, Calories).
 */
@Composable
fun SamsungWorkoutHeroCard(
    distanceKm: String = "7.96",
    activityType: String = "Walking",
    duration: String = "1:41:05",
    steps: Int = 10416,
    caloriesBurned: Int = 568,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val formattedSteps = remember(steps) {
        NumberFormat.getNumberInstance(Locale.US).format(steps)
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEA580C) // Vibrant Samsung Orange
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Large Distance & Activity Name
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = distanceKm,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "km",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                    Text(
                        text = activityType,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f)
                    )
                }
            }

            // Right: 3 Frosted Mini Metric Rows (Timer, Steps, Calories)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                FrostedMetricRow(
                    icon = Icons.Outlined.Timer,
                    text = duration
                )
                FrostedMetricRow(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    text = formattedSteps
                )
                FrostedMetricRow(
                    icon = Icons.Default.LocalFireDepartment,
                    text = "$caloriesBurned kcal"
                )
            }
        }
    }
}

@Composable
private fun FrostedMetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 2. Samsung Health Vivid Green Step Goal Progress Card.
 * Matches Image 1 (Green Card with 10,980 / 10,000 steps, 109% pill, horizontal progress bar).
 */
@Composable
fun SamsungGreenStepGoalCard(
    currentSteps: Int = 10980,
    goalSteps: Int = 10000,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progress = remember(currentSteps, goalSteps) {
        (currentSteps.toFloat() / goalSteps.coerceAtLeast(1))
    }
    val percentage = (progress * 100).toInt()

    val formattedSteps = remember(currentSteps) {
        NumberFormat.getNumberInstance(Locale.US).format(currentSteps)
    }
    val formattedGoal = remember(goalSteps) {
        NumberFormat.getNumberInstance(Locale.US).format(goalSteps)
    }

    val animProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "horizontalProgressAnim"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF009647) // Samsung Vivid Green
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Steps count & Goal
            Column {
                Text(
                    text = formattedSteps,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "/$formattedGoal steps",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Right: Percentage Pill Badge + Horizontal Animated Progress Bar
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Percentage Pill Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.90f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$percentage%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF007A38)
                    )
                }

                // Horizontal Animated Progress Bar
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2ECC71))
                    )
                }
            }
        }
    }
}

/**
 * 3. Samsung One UI 7 Vitals & Cardio Load 2-Column Grid Cards.
 * Matches Image 2 (Vitals with radar ripple, Daily Cardio Load with gauge arc).
 */
@Composable
fun SamsungVitalsCard(
    sleepScore: String = "94 Good",
    subtitle: String = "Track your sleep to measure vitals.",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(155.dp)
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0097A7), Color(0xFF00838F))
                    )
                )
        ) {
            // Radar concentric ripple arc decoration in bottom right
            Canvas(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val cx = size.width
                val cy = size.height
                val rings = listOf(90f, 65f, 40f)
                rings.forEach { r ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.18f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Vitals",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "• $sleepScore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.90f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun SamsungCardioLoadCard(
    status: String = "Optimal",
    subtitle: String = "Find your daily training sweet spot.",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(155.dp)
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E88E5), Color(0xFF1565C0))
                    )
                )
        ) {
            // Speedometer gauge arc decoration in bottom right
            Canvas(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val cx = size.width
                val cy = size.height
                drawArc(
                    color = Color.White.copy(alpha = 0.20f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - 85f, cy - 85f),
                    size = androidx.compose.ui.geometry.Size(170f, 170f),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cardio Load",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "• $status",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }

                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.90f),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

/**
 * 4. Rich One UI 7 Quick Stats Cards (matching Health & Vitals styling).
 */
@Composable
fun SamsungCaloriesCard(
    calories: String = "320",
    subtitle: String = "Active burn on track",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFF6D00), Color(0xFFE65100))
                    )
                )
        ) {
            // Radiant flame ripple arcs in bottom right
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val cx = size.width
                val cy = size.height
                listOf(80f, 55f, 30f).forEach { r ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.18f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Calories",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = calories,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kcal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }
        }
    }
}

@Composable
fun SamsungActiveTimeCard(
    minutes: String = "54",
    subtitle: String = "6m left to daily goal",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF673AB7), Color(0xFF512DA8))
                    )
                )
        ) {
            // Clock dial quarter arc in bottom right
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val cx = size.width
                val cy = size.height
                drawArc(
                    color = Color.White.copy(alpha = 0.20f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - 70f, cy - 70f),
                    size = androidx.compose.ui.geometry.Size(140f, 140f),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Active Time",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = minutes,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "min",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }
        }
    }
}

@Composable
fun SamsungDistanceCard(
    distance: String = "5.8",
    subtitle: String = "7,842 steps covered",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF00897B), Color(0xFF004D40))
                    )
                )
        ) {
            // Milestone trail curves in bottom right
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val cx = size.width
                val cy = size.height
                listOf(75f, 45f).forEach { r ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.18f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Distance",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = distance,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "km",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }
        }
    }
}

@Composable
fun SamsungStairsCard(
    floors: String = "12",
    subtitle: String = "Elevation ~36 meters",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0288D1), Color(0xFF01579B))
                    )
                )
        ) {
            // Ascending staircase arc in bottom right
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
            ) {
                val cx = size.width
                val cy = size.height
                drawArc(
                    color = Color.White.copy(alpha = 0.20f),
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - 75f, cy - 75f),
                    size = androidx.compose.ui.geometry.Size(150f, 150f),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stairs,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Elevation",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = floors,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "floors",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }
        }
    }
}
