package com.fixmax.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamsungDailyActivityDetailSheet(
    onDismiss: () -> Unit,
    steps: Int = 7842,
    activeMinutes: Int = 54,
    caloriesBurned: Int = 320,
    stepsGoal: Int = 10000,
    activeMinutesGoal: Int = 60,
    caloriesGoal: Int = 500
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: < Today > pill with Close & Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF1A2E26))
                }

                Surface(
                    color = Color(0xFFF0F4F2),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A2E26)
                        )
                    }
                }

                Row {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar", tint = Color(0xFF1A2E26))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color(0xFF1A2E26))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday Strip with Mini Heart Rings (S M T W T F S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { idx, day ->
                    val isToday = idx == 4 // Thursday or current day
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = if (isToday) Modifier
                            .background(Color(0xFFE2F5ED), RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                        else Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) Color(0xFF005A43) else Color(0xFF7A9388)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ConcentricActivityRing(
                            stepsProgress = if (idx <= 4) (0.6f + (idx * 0.08f)).coerceIn(0f, 1f) else 0f,
                            activeTimeProgress = if (idx <= 4) (0.5f + (idx * 0.1f)).coerceIn(0f, 1f) else 0f,
                            caloriesProgress = if (idx <= 4) (0.4f + (idx * 0.08f)).coerceIn(0f, 1f) else 0f,
                            size = 28.dp,
                            strokeWidth = 2.5.dp,
                            ringSpacing = 1.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Centerpiece: 3-Layer Concentric Activity Circle Ring
            ConcentricActivityRing(
                stepsProgress = steps.toFloat() / stepsGoal,
                activeTimeProgress = activeMinutes.toFloat() / activeMinutesGoal,
                caloriesProgress = caloriesBurned.toFloat() / caloriesGoal,
                size = 200.dp,
                strokeWidth = 14.dp,
                ringSpacing = 4.dp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3 Clean Metric Cards (Steps, Active Time, Activity Cal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SamsungMetricPill(
                    title = "Steps",
                    titleColor = Color(0xFF2ECC71),
                    value = steps.toString(),
                    goal = "/$stepsGoal",
                    modifier = Modifier.weight(1f)
                )
                SamsungMetricPill(
                    title = "Active time",
                    titleColor = Color(0xFF00B4D8),
                    value = "$activeMinutes",
                    goal = "/$activeMinutesGoal mins",
                    modifier = Modifier.weight(1f)
                )
                SamsungMetricPill(
                    title = "Activity cal",
                    titleColor = Color(0xFFFF2D75),
                    value = "$caloriesBurned",
                    goal = "/$caloriesGoal Cal",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 24-Hour Activity Timeline
            SamsungActivityTimeline()

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom summary rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total burned calories", fontSize = 13.sp, color = Color(0xFF6B867B))
                Text(text = "1,850 Cal", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Distance while active", fontSize = 13.sp, color = Color(0xFF6B867B))
                Text(text = "5.8 km", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }
}

@Composable
private fun SamsungMetricPill(
    title: String,
    titleColor: Color,
    value: String,
    goal: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBF9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A2E26)
            )
            Text(
                text = goal,
                fontSize = 10.sp,
                color = Color(0xFF7A9388)
            )
        }
    }
}
