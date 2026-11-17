package com.fixmax.ui.components

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import com.fixmax.data.repository.HealthMetricsEngine

data class WeekdayActivityRecord(
    val dayLetter: String,
    val fullDate: String,
    val steps: Int,
    val activeMinutes: Int,
    val caloriesBurned: Int,
    val totalBurned: String,
    val distance: String,
    val isToday: Boolean = false
)

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
    val context = LocalContext.current

    val weekData = remember(steps, activeMinutes, caloriesBurned) {
        val history = HealthMetricsEngine.generateWeeklyHistory(steps.toLong())
        val todayIdx = HealthMetricsEngine.TODAY_INDEX
        val startIndex = (todayIdx - 3).coerceAtLeast(0)
        val slice = history.subList(startIndex, (startIndex + 7).coerceAtMost(history.size))
        slice.mapIndexed { idx, s ->
            val isToday = idx == 3
            WeekdayActivityRecord(
                dayLetter = s.dayLabel.first().toString(),
                fullDate = if (isToday) "Today (${s.dayLabel}, Nov ${s.dateNumber})" else "${s.dayLabel}, Nov ${s.dateNumber}",
                steps = s.steps.toInt(),
                activeMinutes = s.activeMinutes,
                caloriesBurned = s.caloriesBurned,
                totalBurned = "${NumberFormat.getNumberInstance(Locale.US).format(s.caloriesBurned + 1400)} Cal",
                distance = s.formattedDistance,
                isToday = isToday
            )
        }
    }

    var selectedDayIdx by remember { mutableIntStateOf(3) }
    var showDatePicker by remember { mutableStateOf(false) }

    val activeRecord = weekData[selectedDayIdx.coerceIn(weekData.indices)]

    val animatedStepsProgress by animateFloatAsState(
        targetValue = (activeRecord.steps.toFloat() / stepsGoal).coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "stepsProg"
    )
    val animatedTimeProgress by animateFloatAsState(
        targetValue = (activeRecord.activeMinutes.toFloat() / activeMinutesGoal).coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "timeProg"
    )
    val animatedCalProgress by animateFloatAsState(
        targetValue = (activeRecord.caloriesBurned.toFloat() / caloriesGoal).coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "calProg"
    )

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
            // Top Bar: < Today / Date > pill with Close & Working Calendar / Share Buttons
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
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        selectedDayIdx = 3 // Reset to Today
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedDayIdx == 3) "Today" else activeRecord.fullDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A2E26)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Working Calendar Button (Opens Date Picker Dialog)
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.m3BounceClick { showDatePicker = true }
                    ) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar", tint = Color(0xFF1A2E26))
                    }

                    // Working Share Button (Native Android Share Sheet)
                    IconButton(
                        onClick = {
                            val headerTitle = if (selectedDayIdx == 4) "Today" else activeRecord.fullDate
                            val shareBody = """
                                🏃 FitMax Daily Activity Summary ($headerTitle)
                                👣 Steps: ${NumberFormat.getNumberInstance(Locale.US).format(activeRecord.steps)} / $stepsGoal
                                ⏱ Active Time: ${activeRecord.activeMinutes} / $activeMinutesGoal mins
                                🔥 Activity Calories: ${activeRecord.caloriesBurned} / $caloriesGoal Cal
                                📊 Total Burned: ${activeRecord.totalBurned}
                                📍 Distance: ${activeRecord.distance}
                                
                                Tracked with FitMax on Samsung Galaxy
                            """.trimIndent()

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Daily Activity")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.m3BounceClick {
                            val headerTitle = if (selectedDayIdx == 4) "Today" else activeRecord.fullDate
                            val shareBody = """
                                🏃 FitMax Daily Activity Summary ($headerTitle)
                                👣 Steps: ${NumberFormat.getNumberInstance(Locale.US).format(activeRecord.steps)} / $stepsGoal
                                ⏱ Active Time: ${activeRecord.activeMinutes} / $activeMinutesGoal mins
                                🔥 Activity Calories: ${activeRecord.caloriesBurned} / $caloriesGoal Cal
                                📊 Total Burned: ${activeRecord.totalBurned}
                                📍 Distance: ${activeRecord.distance}
                                
                                Tracked with FitMax on Samsung Galaxy
                            """.trimIndent()

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Daily Activity")
                            context.startActivity(shareIntent)
                        }
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color(0xFF1A2E26))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fully Working Weekday Strip with Mini Concentric Rings (S M T W T F S)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekData.forEachIndexed { idx, item ->
                    val isSelected = idx == selectedDayIdx
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedDayIdx = idx
                            }
                            .then(
                                if (isSelected) Modifier
                                    .background(Color(0xFFE2F5ED), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                else Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                    ) {
                        Text(
                            text = item.dayLetter,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF005A43) else Color(0xFF7A9388)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ConcentricActivityRing(
                            stepsProgress = (item.steps.toFloat() / stepsGoal).coerceIn(0f, 1f),
                            activeTimeProgress = (item.activeMinutes.toFloat() / activeMinutesGoal).coerceIn(0f, 1f),
                            caloriesProgress = (item.caloriesBurned.toFloat() / caloriesGoal).coerceIn(0f, 1f),
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
                stepsProgress = animatedStepsProgress,
                activeTimeProgress = animatedTimeProgress,
                caloriesProgress = animatedCalProgress,
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
                    value = NumberFormat.getNumberInstance(Locale.US).format(activeRecord.steps),
                    goal = "/$stepsGoal",
                    modifier = Modifier.weight(1f)
                )
                SamsungMetricPill(
                    title = "Active time",
                    titleColor = Color(0xFF00B4D8),
                    value = "${activeRecord.activeMinutes}",
                    goal = "/$activeMinutesGoal mins",
                    modifier = Modifier.weight(1f)
                )
                SamsungMetricPill(
                    title = "Activity cal",
                    titleColor = Color(0xFFFF2D75),
                    value = "${activeRecord.caloriesBurned}",
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
                Text(text = activeRecord.totalBurned, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Distance while active", fontSize = 13.sp, color = Color(0xFF6B867B))
                Text(text = activeRecord.distance, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            }

            Spacer(modifier = Modifier.height(34.dp))
        }
    }

    // FitMax Custom Calendar Modal Bottom Sheet
    if (showDatePicker) {
        FitMaxCustomCalendarSheet(
            initialDate = java.time.LocalDate.now(),
            onDismiss = { showDatePicker = false },
            onDateSelected = { _ ->
                showDatePicker = false
            }
        )
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
