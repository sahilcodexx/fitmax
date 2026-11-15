package com.fixmax.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Samsung Health Daily Activity Overview Card.
 * Displays 3 metric badges (Steps, Active Time, Calories) alongside the animated Heart Activity Ring.
 */
@Composable
fun SamsungDailyActivityCard(
    steps: Int = 7842,
    activeMinutes: Int = 54,
    caloriesBurned: Int = 320,
    stepsGoal: Int = 10000,
    activeMinutesGoal: Int = 60,
    caloriesGoal: Int = 500,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val formattedSteps = remember(steps) {
        java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(steps)
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF5EA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Activity",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF005A43)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "↑ 12% than yesterday",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF005A43)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFCCEEDF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = Color(0xFF005A43),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 3 Badges Column (Steps, Active Time, Calories)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SamsungBadgeRow(
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        value = "$formattedSteps steps",
                        bgColor = Color(0xFF2ECC71),
                        textColor = Color(0xFF1A2E26)
                    )
                    SamsungBadgeRow(
                        icon = Icons.Default.Schedule,
                        value = "$activeMinutes min active",
                        bgColor = Color(0xFF00B4D8),
                        textColor = Color(0xFF1A2E26)
                    )
                    SamsungBadgeRow(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "$caloriesBurned kcal burned",
                        bgColor = Color(0xFFFF2D75),
                        textColor = Color(0xFF1A2E26)
                    )
                }

                // Right: 3-Layer Concentric Activity Circle Ring (Circle Bar)
                ConcentricActivityRing(
                    stepsProgress = steps.toFloat() / stepsGoal,
                    activeTimeProgress = activeMinutes.toFloat() / activeMinutesGoal,
                    caloriesProgress = caloriesBurned.toFloat() / caloriesGoal,
                    size = 100.dp,
                    strokeWidth = 8.dp,
                    ringSpacing = 3.dp
                )
            }
        }
    }
}

@Composable
private fun SamsungBadgeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    bgColor: Color,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Samsung Health Exercise Quick Launch Bar.
 * Circular action buttons for Walk, Run, Cycling, and Log.
 */
@Composable
fun SamsungExerciseBar(
    onWalkClick: () -> Unit = {},
    onRunClick: () -> Unit = {},
    onCycleClick: () -> Unit = {},
    onLogClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercise",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2E26)
                )
                Text(
                    text = "View log",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF005A43)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseCircleButton(
                    icon = Icons.Default.DirectionsWalk,
                    label = "Walk",
                    onClick = onWalkClick
                )
                ExerciseCircleButton(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    label = "Run",
                    onClick = onRunClick
                )
                ExerciseCircleButton(
                    icon = Icons.Default.DirectionsBike,
                    label = "Cycling",
                    onClick = onCycleClick
                )
                ExerciseCircleButton(
                    icon = Icons.Default.FormatListBulleted,
                    label = "Log",
                    onClick = onLogClick
                )
            }
        }
    }
}

@Composable
private fun ExerciseCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.m3BounceClick(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(0xFFF3F7F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF1A2E26),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B867B),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 24-Hour Samsung Activity Distribution Timeline Bar.
 */
@Composable
fun SamsungActivityTimeline(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Baseline track
                drawLine(
                    color = Color(0xFFE2EBE6),
                    start = Offset(0f, h / 2f),
                    end = Offset(w, h / 2f),
                    strokeWidth = 2.dp.toPx()
                )

                // Activity ticks representing movement intervals
                val sampleTicks = listOf(0.12f, 0.22f, 0.28f, 0.35f, 0.42f, 0.48f, 0.55f, 0.60f)
                sampleTicks.forEach { fraction ->
                    val x = w * fraction
                    val barH = if (fraction > 0.40f) 12.dp.toPx() else 7.dp.toPx()
                    drawLine(
                        color = Color(0xFF2ECC71),
                        start = Offset(x, h / 2f - barH / 2f),
                        end = Offset(x, h / 2f + barH / 2f),
                        strokeWidth = 2.5.dp.toPx()
                    )
                }

                // "Now" indicator dot
                val nowX = w * 0.62f
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(nowX, h / 2f))
                drawCircle(color = Color(0xFF1A2E26), radius = 3.dp.toPx(), center = Offset(nowX, h / 2f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("12 am", "6 am", "Now", "6 pm", "12 am").forEach { tLabel ->
                Text(
                    text = tLabel,
                    fontSize = 11.sp,
                    color = if (tLabel == "Now") Color(0xFF1A2E26) else Color(0xFF7A9388),
                    fontWeight = if (tLabel == "Now") FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
