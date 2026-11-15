package com.fixmax.feature.steps

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.ui.components.CircularProgressRing
import com.fixmax.ui.components.HourlyBarChart
import com.fixmax.ui.components.m3BounceClick
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsScreen(
    onBack: () -> Unit = {}
) {
    var selectedTimeframe by remember { mutableStateOf("1D") }
    val timeframes = listOf("1D", "1W", "1M", "3M", "1Y")

    // Interactive Dialog States
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("Today, ${SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())}") }
    var showDistanceDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var dailyGoalTarget by remember { mutableStateOf(8500) }

    // Dynamic metrics based on timeframe
    val (headlineSteps, trendText, distanceText, chartData, chartLabels, peakIndex, peakTooltip) = when (selectedTimeframe) {
        "1D" -> TimeframeData(
            steps = "7,842",
            trend = "↑ 12% than yesterday",
            distance = "5.8 km",
            bars = listOf(
                0.05f, 0.02f, 0.08f, 0.20f, 0.35f, 0.50f, 0.25f, 0.40f, 0.65f, 0.30f,
                0.55f, 0.75f, 0.45f, 0.85f, 0.60f, 1.00f, 0.70f, 0.80f, 0.55f, 0.65f, 0.40f
            ),
            labels = listOf("12 AM", "6 AM", "12 PM", "6 PM", "12 AM"),
            peakIdx = 15,
            peakTip = "1.2k"
        )
        "1W" -> TimeframeData(
            steps = "54,230",
            trend = "↑ 8% vs last week",
            distance = "40.1 km",
            bars = listOf(0.60f, 0.75f, 0.50f, 0.90f, 0.80f, 0.65f, 1.00f),
            labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
            peakIdx = 6,
            peakTip = "11.2k"
        )
        "1M" -> TimeframeData(
            steps = "234,100",
            trend = "↑ 15% vs last month",
            distance = "173.2 km",
            bars = listOf(
                0.4f, 0.6f, 0.8f, 0.5f, 0.7f, 0.9f, 0.85f, 0.65f, 0.5f, 0.75f,
                0.8f, 1.0f, 0.7f, 0.65f, 0.9f, 0.55f, 0.7f, 0.85f, 0.6f, 0.8f
            ),
            labels = listOf("Day 1", "Day 8", "Day 15", "Day 22", "Day 30"),
            peakIdx = 11,
            peakTip = "14.5k"
        )
        "3M" -> TimeframeData(
            steps = "698,400",
            trend = "↑ 5% vs last quarter",
            distance = "516.8 km",
            bars = listOf(0.55f, 0.70f, 0.65f, 0.80f, 0.75f, 0.90f, 0.85f, 0.70f, 0.95f, 1.00f, 0.80f, 0.90f),
            labels = listOf("W1", "W4", "W7", "W10", "W12"),
            peakIdx = 9,
            peakTip = "65k"
        )
        else -> TimeframeData(
            steps = "2.84M",
            trend = "↑ 18% vs last year",
            distance = "2,103 km",
            bars = listOf(0.6f, 0.65f, 0.75f, 0.8f, 0.7f, 0.85f, 0.9f, 0.95f, 0.8f, 0.75f, 0.9f, 1.0f),
            labels = listOf("Jan", "Mar", "Jun", "Sep", "Dec"),
            peakIdx = 11,
            peakTip = "260k"
        )
    }

    val goalPercent = remember(dailyGoalTarget) {
        ((7842f / dailyGoalTarget) * 100).toInt().coerceIn(0, 100)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FCF9))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top App Bar: Back, "Steps", Calendar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.m3BounceClick { onBack() }
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1A2E26))
            }
            Text(text = "Steps", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            IconButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.m3BounceClick { showDatePicker = true }
            ) {
                Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = "Calendar", tint = Color(0xFF1A2E26))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Timeframe Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            timeframes.forEach { tf ->
                val isSelected = tf == selectedTimeframe
                Button(
                    onClick = { selectedTimeframe = tf },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF005A43) else Color.Transparent,
                        contentColor = if (isSelected) Color.White else Color(0xFF6B867B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .m3BounceClick { selectedTimeframe = tf }
                ) {
                    Text(text = tf, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline Steps Display with AnimatedContent
        AnimatedContent(
            targetState = headlineSteps to trendText,
            transitionSpec = {
                (fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInVertically(tween(240)) { it / 8 }) togetherWith
                    (fadeOut(tween(120)) + slideOutVertically(tween(180)) { -it / 8 })
            },
            label = "headlineStepsAnim"
        ) { (steps, trend) ->
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = steps, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A2E26))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "steps", fontSize = 18.sp, color = Color(0xFF6B867B), modifier = Modifier.padding(bottom = 6.dp))
                }
                Text(text = trend, fontSize = 12.sp, color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Interactive Animated Bar Chart
        HourlyBarChart(
            data = chartData,
            labels = chartLabels,
            peakIndex = peakIndex,
            peakValueText = peakTooltip
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Distance Card: "That's about X km >"
        Card(
            onClick = { showDistanceDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .m3BounceClick { showDistanceDialog = true },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F7EE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFD0F0E0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFF005A43))
                    }
                    Column {
                        Text(text = "That's about", fontSize = 12.sp, color = Color(0xFF6B867B))
                        Text(text = distanceText, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
                    }
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF005A43))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Insights Section
        Text(text = "Insights", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = { showGoalDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .m3BounceClick { showGoalDialog = true },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFF0EBF9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎯", fontSize = 18.sp)
                    }
                    Text(
                        text = "You're $goalPercent% close to\nyour daily goal!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2E26)
                    )
                }

                CircularProgressRing(progressPercent = goalPercent)
            }
        }

        Spacer(modifier = Modifier.height(130.dp))
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            selectedDateText = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(cal.time)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0xFF6B867B))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF005A43),
                    todayDateBorderColor = Color(0xFF005A43),
                    todayContentColor = Color(0xFF005A43)
                )
            )
        }
    }

    // Distance Detail Dialog
    if (showDistanceDialog) {
        AlertDialog(
            onDismissRequest = { showDistanceDialog = false },
            confirmButton = {
                TextButton(onClick = { showDistanceDialog = false }) {
                    Text("Got it", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("Distance Breakdown ($distanceText)", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Average Cadence: 106 steps/minute", color = Color(0xFF1A2E26), fontSize = 14.sp)
                    Text("• Dynamic Stride Length: 0.74 meters", color = Color(0xFF1A2E26), fontSize = 14.sp)
                    Text("• Pace: 11'24\" / km", color = Color(0xFF1A2E26), fontSize = 14.sp)
                    Text("• Calorie Burn Ratio: 55 kcal / km", color = Color(0xFF005A43), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Goal Settings Dialog
    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            confirmButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Save", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("Adjust Daily Step Goal", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Choose your daily step milestone:", fontSize = 14.sp, color = Color(0xFF6B867B))
                    listOf(6000, 8500, 10000, 12000).forEach { goal ->
                        val isSelected = goal == dailyGoalTarget
                        FilterChip(
                            selected = isSelected,
                            onClick = { dailyGoalTarget = goal },
                            label = { Text("$goal steps/day") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDCF4E9),
                                selectedLabelColor = Color(0xFF005A43)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

private data class TimeframeData(
    val steps: String,
    val trend: String,
    val distance: String,
    val bars: List<Float>,
    val labels: List<String>,
    val peakIdx: Int,
    val peakTip: String
)
