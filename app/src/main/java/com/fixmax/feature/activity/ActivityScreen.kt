package com.fixmax.feature.activity

import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.ui.components.BezierSplineChart
import com.fixmax.ui.components.ConcentricActivityRing
import com.fixmax.ui.components.SamsungActivityTimeline
import com.fixmax.ui.components.m3BounceClick

@Composable
fun ActivityScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("Steps") }
    val filters = listOf("Steps", "Calories", "Active Time")

    var showMenu by remember { mutableStateOf(false) }
    var showSensorLabDialog by remember { mutableStateOf(false) }
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var selectedSensitivity by remember { mutableStateOf("Normal") }
    var selectedSummaryDetail by remember { mutableStateOf<String?>(null) }

    val (headlineValue, headlineUnit, trendText, splineData, splineTooltip) = when (selectedFilter) {
        "Steps" -> MetricData(
            value = "7,842",
            unit = "steps",
            trend = "↑ 12% than yesterday",
            points = listOf(0.60f, 0.50f, 0.40f, 0.65f, 0.45f, 0.70f, 0.90f),
            tip = "7.8k"
        )
        "Calories" -> MetricData(
            value = "2,140",
            unit = "kcal",
            trend = "↑ 8% burned than yesterday",
            points = listOf(0.45f, 0.55f, 0.60f, 0.50f, 0.70f, 0.85f, 0.95f),
            tip = "420 kcal"
        )
        else -> MetricData(
            value = "6h 12m",
            unit = "active",
            trend = "↑ 15% more active this week",
            points = listOf(0.50f, 0.65f, 0.45f, 0.75f, 0.60f, 0.80f, 0.90f),
            tip = "68 min"
        )
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

        // Top Bar: Back, "Activity", More Options
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
            Text(text = "Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.m3BounceClick { showMenu = true }
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF1A2E26))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sensor Diagnostics Lab") },
                        onClick = {
                            showMenu = false
                            showSensorLabDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF005A43)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Pedometer Sensitivity") },
                        onClick = {
                            showMenu = false
                            showSensitivityDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF005A43)) }
                    )
                    DropdownMenuItem(
                        text = { Text("Export Activity Log (CSV)") },
                        onClick = {
                            showMenu = false
                            Toast.makeText(context, "Activity exported to Downloads/fitmax_activity.csv", Toast.LENGTH_LONG).show()
                        },
                        leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF005A43)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Samsung Health Weekday Heart Strip (S M T W T F S)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { idx, day ->
                val isToday = idx == 4
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

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Chips (Steps / Calories / Active Time)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = filter == selectedFilter
                Button(
                    onClick = { selectedFilter = filter },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF005A43) else Color.Transparent,
                        contentColor = if (isSelected) Color.White else Color(0xFF6B867B)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .m3BounceClick { selectedFilter = filter }
                ) {
                    Text(text = filter, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Step / Metric Display with AnimatedContent
        AnimatedContent(
            targetState = Triple(headlineValue, headlineUnit, trendText),
            transitionSpec = {
                (fadeIn(tween(180, easing = FastOutSlowInEasing)) + slideInVertically(tween(240)) { it / 8 }) togetherWith
                    (fadeOut(tween(120)) + slideOutVertically(tween(180)) { -it / 8 })
            },
            label = "metricHeadlineAnim"
        ) { (value, unit, trend) ->
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = value, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A2E26))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = unit, fontSize = 18.sp, color = Color(0xFF6B867B), modifier = Modifier.padding(bottom = 6.dp))
                }
                Text(text = trend, fontSize = 12.sp, color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Smooth Bezier Spline Chart (Mon-Sun)
        BezierSplineChart(
            dataPoints = splineData,
            peakValueText = splineTooltip
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Samsung 24-Hour Activity Timeline
        SamsungActivityTimeline()

        Spacer(modifier = Modifier.height(28.dp))

        // Weekly Summary Header
        Text(text = "Weekly Summary", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
        Spacer(modifier = Modifier.height(14.dp))

        // 2x2 Grid Summary (All Interactive)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WeeklySummaryCard(
                icon = Icons.Default.DirectionsRun,
                iconTint = Color(0xFF005A43),
                iconBg = Color(0xFFE2F5ED),
                value = "48.2k",
                label = "Total Steps",
                modifier = Modifier.weight(1f),
                onClick = { selectedSummaryDetail = "Total Steps" }
            )
            WeeklySummaryCard(
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFFFF6D00),
                iconBg = Color(0xFFFFF0E6),
                value = "2.1k",
                label = "Total Calories",
                modifier = Modifier.weight(1f),
                onClick = { selectedSummaryDetail = "Total Calories" }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WeeklySummaryCard(
                icon = Icons.Default.Schedule,
                iconTint = Color(0xFF5E35B1),
                iconBg = Color(0xFFF0EBF9),
                value = "6h 12m",
                label = "Active Time",
                modifier = Modifier.weight(1f),
                onClick = { selectedSummaryDetail = "Active Time" }
            )
            WeeklySummaryCard(
                icon = Icons.Default.Place,
                iconTint = Color(0xFF00897B),
                iconBg = Color(0xFFE0F2F1),
                value = "32.5 km",
                label = "Distance",
                modifier = Modifier.weight(1f),
                onClick = { selectedSummaryDetail = "Distance" }
            )
        }

        Spacer(modifier = Modifier.height(130.dp))
    }

    // Sensor Diagnostics Dialog
    if (showSensorLabDialog) {
        AlertDialog(
            onDismissRequest = { showSensorLabDialog = false },
            confirmButton = {
                TextButton(onClick = { showSensorLabDialog = false }) {
                    Text("Close", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("Sensor Diagnostics Lab", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Hardware Step Counter: Active (Samsung Hub)", color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("• Accelerometer DSP: Standby Fallback", color = Color(0xFF1A2E26), fontSize = 13.sp)
                    Text("• Sampling Rate: 50 Hz (Standard Sensor Delay)", color = Color(0xFF1A2E26), fontSize = 13.sp)
                    Text("• Battery Impact: Ultra Low (< 0.8% / 24h)", color = Color(0xFF005A43), fontSize = 13.sp)
                    Text("• Background Service: StepTrackingService Active", color = Color(0xFF6B867B), fontSize = 12.sp)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Pedometer Sensitivity Dialog
    if (showSensitivityDialog) {
        AlertDialog(
            onDismissRequest = { showSensitivityDialog = false },
            confirmButton = {
                TextButton(onClick = { showSensitivityDialog = false }) {
                    Text("Apply", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("Pedometer Sensitivity", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configure accelerometer threshold for step filtering:", fontSize = 13.sp, color = Color(0xFF6B867B))
                    listOf("High (Light walking)", "Normal (Recommended)", "Low (Ignore vehicle bumps)").forEach { opt ->
                        val isSelected = opt.startsWith(selectedSensitivity)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSensitivity = opt.substringBefore(" ") },
                            label = { Text(opt) },
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

    // Summary Card Detail Dialog
    selectedSummaryDetail?.let { detail ->
        AlertDialog(
            onDismissRequest = { selectedSummaryDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedSummaryDetail = null }) {
                    Text("Close", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text("$detail Insights", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (detail) {
                        "Total Steps" -> {
                            Text("• 7-Day Total: 48,230 steps", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Daily Average: 6,890 steps/day", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Peak Day: Sunday (11,420 steps)", color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        "Total Calories" -> {
                            Text("• Total Burned: 2,140 kcal", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Equivalent to: ~4 healthy meals burned", color = Color(0xFF005A43), fontSize = 13.sp)
                            Text("• Active Burn Rate: 305 kcal/day", color = Color(0xFF6B867B), fontSize = 13.sp)
                        }
                        "Active Time" -> {
                            Text("• Total Active: 6 hours 12 minutes", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Brisk Walking: 4 hours 25 minutes", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• WHO Health Target: Reached 148% of recommended activity", color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        "Distance" -> {
                            Text("• Cumulative Distance: 32.5 km", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Track Equivalent: 81 standard laps", color = Color(0xFF005A43), fontSize = 13.sp)
                            Text("• Elevation Gain: 120 meters", color = Color(0xFF6B867B), fontSize = 13.sp)
                        }
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun WeeklySummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.m3BounceClick(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
                Text(text = label, fontSize = 11.sp, color = Color(0xFF6B867B))
            }
        }
    }
}

private data class MetricData(
    val value: String,
    val unit: String,
    val trend: String,
    val points: List<Float>,
    val tip: String
)
