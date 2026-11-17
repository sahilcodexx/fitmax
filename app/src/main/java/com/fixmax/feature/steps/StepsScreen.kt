package com.fixmax.feature.steps

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.data.repository.HealthMetricsEngine
import com.fixmax.tracking.StepTrackingService
import com.fixmax.ui.components.FitMaxCustomCalendarSheet
import com.fixmax.ui.components.m3BounceClick
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Steps & Daily Activity Screen.
 * 1. Horizontal swipeable/scrollable date carousel with edge fade & smooth animated selection (Image 1)
 * 2. Animated rolling counter for hero metric (when scrolling/switching)
 * 3. Nutrition | Activity toggle switcher
 * 4. 2-Column metric cards (Distance 6.7 km, Active Time 1h 12m)
 * 5. Daily Updates:
 *    - Oxygen card (84,3 %, +2% Normal, coral soundwave equalizer canvas)
 *    - Hart Rate card (102 bpm, -7% Low, coral ECG heartbeat wave canvas)
 * 6. Health Improvement Score card (54%, Normal)
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun StepsScreen(
    onBack: () -> Unit = {}
) {
    val liveSteps by StepTrackingService.liveStepsToday.collectAsState()
    val baseSteps = if (liveSteps > 0) liveSteps else 1947L

    var referenceDate by remember { mutableStateOf(LocalDate.now()) }

    // Dynamic full date history (past 30 days + future 14 days, today at index 30)
    val weeklyHistory = remember(baseSteps, referenceDate) {
        HealthMetricsEngine.generateWeeklyHistory(baseSteps, referenceDate)
    }

    var selectedDayIndex by remember { mutableIntStateOf(HealthMetricsEngine.TODAY_INDEX) }
    var selectedMode by remember { mutableStateOf("Activity") } // "Nutrition" or "Activity"
    var showMetricDialog by remember { mutableStateOf<String?>(null) }
    var showCustomCalendarSheet by remember { mutableStateOf(false) }

    val (weeklyPointsTotal, weeklyPointsPercent) = remember(weeklyHistory, selectedDayIndex) {
        HealthMetricsEngine.computeWeeklyPoints(weeklyHistory, selectedDayIndex)
    }

    val dateList = remember(weeklyHistory) {
        weeklyHistory.map { snapshot ->
            Triple(snapshot.dayLabel, snapshot.dateNumber, snapshot.steps.toInt())
        }
    }

    val currentSnapshot = weeklyHistory[selectedDayIndex.coerceIn(weeklyHistory.indices)]

    // Target values for animated counter
    val targetNumber = if (selectedMode == "Nutrition") currentSnapshot.caloriesBurned else currentSnapshot.steps.toInt()

    // Animated Rolling Counter
    val animatedHeroNumber by animateIntAsState(
        targetValue = targetNumber,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "heroNumberRoll"
    )

    val formattedNumber = remember(animatedHeroNumber) {
        NumberFormat.getNumberInstance(Locale.US).format(animatedHeroNumber)
    }

    val coroutineScope = rememberCoroutineScope()
    // Animatable float index for buttery smooth drag & snap (starts at TODAY_INDEX: 30f)
    val animIndex = remember { Animatable(HealthMetricsEngine.TODAY_INDEX.toFloat()) }
    val density = LocalDensity.current
    val itemSpacingPx = remember(density) { with(density) { 74.dp.toPx() } }
    val itemWidthPx = remember(density) { with(density) { 60.dp.toPx() } }
    val maxDipPx = remember(density) { with(density) { 36.dp.toPx() } }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. C-Type Circular Curved Date Wheel (Guaranteed Dead Center + Silky Smooth Drag)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val targetIndex = animIndex.value.roundToInt().coerceIn(dateList.indices)
                            coroutineScope.launch {
                                animIndex.animateTo(
                                    targetValue = targetIndex.toFloat(),
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                    )
                                )
                            }
                            selectedDayIndex = targetIndex
                        },
                        onDragCancel = {
                            val targetIndex = animIndex.value.roundToInt().coerceIn(dateList.indices)
                            coroutineScope.launch {
                                animIndex.animateTo(targetValue = targetIndex.toFloat())
                            }
                            selectedDayIndex = targetIndex
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newIndex = (animIndex.value - dragAmount / itemSpacingPx)
                                .coerceIn(0f, (dateList.size - 1).toFloat())
                            coroutineScope.launch {
                                animIndex.snapTo(newIndex)
                            }
                            val currentClosest = newIndex.roundToInt().coerceIn(dateList.indices)
                            if (currentClosest != selectedDayIndex) {
                                selectedDayIndex = currentClosest
                            }
                        }
                    )
                }
        ) {
            val containerWidthPx = constraints.maxWidth.toFloat()
            val centerX = containerWidthPx / 2f

            dateList.forEachIndexed { index, (dayName, dateNum, _) ->
                val relOffset = (index.toFloat() - animIndex.value)
                if (abs(relOffset) <= 2.8f) {
                    val normDist = (relOffset / 2.0f).coerceIn(-1.5f, 1.5f)
                    val curveFactor = (1f - (normDist * normDist)).coerceIn(0f, 1f)
                    val yOffset = maxDipPx * curveFactor
                    val xPos = centerX + (relOffset * itemSpacingPx) - (itemWidthPx / 2f)
                    val isCurrentCenter = index == animIndex.value.roundToInt()

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(60.dp)
                            .graphicsLayer {
                                translationX = xPos
                                translationY = yOffset
                                val scale = 0.84f + (curveFactor * 0.30f)
                                scaleX = scale
                                scaleY = scale
                                alpha = (0.32f + (curveFactor * 0.68f)).coerceIn(0.15f, 1f)
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (isCurrentCenter) {
                                    // Clicking the centered date opens the custom FitMax Calendar
                                    showCustomCalendarSheet = true
                                } else {
                                    coroutineScope.launch {
                                        animIndex.animateTo(
                                            targetValue = index.toFloat(),
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                                    }
                                    selectedDayIndex = index
                                }
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrentCenter) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isCurrentCenter) Color(0xFF111417) else Color(0xFF888F98)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dateNum,
                            fontSize = if (isCurrentCenter) 36.sp else 30.sp,
                            fontWeight = if (isCurrentCenter) FontWeight.Black else FontWeight.Bold,
                            color = if (isCurrentCenter) Color(0xFF111417) else Color(0xFF888F98),
                            letterSpacing = (-0.5).sp
                        )
                    }
                }
            }

            // Left Edge Gradient Vignette Fade
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(115.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFF7F8FA),
                                Color(0xFFF7F8FA).copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Right Edge Gradient Vignette Fade
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(115.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFF7F8FA).copy(alpha = 0.75f),
                                Color(0xFFF7F8FA)
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Nutrition | Activity Centered Toggle Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFE9EEEC),
                modifier = Modifier.height(42.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeTabButton(
                        title = "Nutrition",
                        isSelected = selectedMode == "Nutrition",
                        onClick = { selectedMode = "Nutrition" }
                    )

                    ModeTabButton(
                        title = "Activity",
                        isSelected = selectedMode == "Activity",
                        onClick = { selectedMode = "Activity" }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Massive Hero Metric with Smooth Rolling Number Animation
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formattedNumber,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111417),
                letterSpacing = (-1.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (selectedMode == "Nutrition") "Calories" else "Steps",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111417)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 2-Column Metric Cards (Distance & Active Time)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Distance Card
            SummaryMetricCard(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = "Distance",
                        tint = Color(0xFF111417),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Distance",
                value = currentSnapshot.formattedDistance,
                onClick = { showMetricDialog = "Distance" },
                modifier = Modifier.weight(1f)
            )

            // Active Time Card
            SummaryMetricCard(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.FitnessCenter,
                        contentDescription = "Active Time",
                        tint = Color(0xFF111417),
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = "Active Time",
                value = currentSnapshot.formattedActiveTime,
                onClick = { showMetricDialog = "Active Time" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Image 2: Workout Tasks Grid (Time & Exercises) - Dynamic Math
        val activeMins = currentSnapshot.activeMinutes
        val formattedTimeHours = String.format(Locale.US, "%d:%02d", activeMins / 60, activeMins % 60)
        WorkoutTasksGrid(
            timeHours = formattedTimeHours,
            exerciseTasks = "${(currentSnapshot.activeMinutes / 10).coerceAtLeast(1)}/8",
            onClickTime = { showMetricDialog = "Workout Time" },
            onClickExercises = { showMetricDialog = "Completed Tasks" }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Image 2: Weekly Points Card with 64% Ring Badge and Smooth Line Graph - Dynamic Math
        WeeklyPointsGraphCard(
            points = weeklyPointsTotal,
            percentage = weeklyPointsPercent,
            delta = "+6%",
            onClick = { showMetricDialog = "Weekly Points" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Section Header: "Daily Updates"
        Text(
            text = "Daily Updates",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111417)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. 2-Column Daily Updates Cards (Oxygen & Hart Rate) - Dynamic Math
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Oxygen Card
            OxygenUpdateCard(
                percentage = String.format(Locale.US, "%.1f %%", currentSnapshot.oxygenPercent),
                delta = "+2%",
                status = "Normal",
                onClick = { showMetricDialog = "Oxygen" },
                modifier = Modifier.weight(1f)
            )

            // Hart Rate Card
            HeartRateUpdateCard(
                bpm = "${currentSnapshot.heartRateBpm} bpm",
                delta = "-7%",
                status = "Low",
                onClick = { showMetricDialog = "Heart Rate" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Health Improvement Score Card - Dynamic Math
        HealthImprovementScoreCard(
            score = "${currentSnapshot.healthScore}%",
            status = if (currentSnapshot.healthScore >= 75) "Optimal" else "Normal",
            description = "This indicator is based on daily analysis of your physiological markers and provides an objective assessment of your overall wellness and vitality.",
            onClick = { showMetricDialog = "Health Improvement Score" }
        )

        Spacer(modifier = Modifier.height(110.dp))
    }

    // Interactive Metric Breakdown Dialog
    showMetricDialog?.let { title ->
        AlertDialog(
            onDismissRequest = { showMetricDialog = null },
            confirmButton = {
                TextButton(onClick = { showMetricDialog = null }) {
                    Text("Close", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF111417))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (title) {
                        "Workout Time" -> {
                            Text("• Total Activity Duration: ${currentSnapshot.formattedActiveTime}", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Active Minutes: ${currentSnapshot.activeMinutes} mins", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Target Goal: ${currentSnapshot.goalActiveMinutes} mins", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Completed Tasks" -> {
                            Text("• Exercises Completed: ${(currentSnapshot.activeMinutes / 10).coerceAtLeast(1)} of 8 tasks", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Calories Burned: ${currentSnapshot.caloriesBurned} kcal", color = Color(0xFF2E7D32), fontSize = 13.sp)
                            Text("• Intensity: Dynamic cardio cadence", color = Color(0xFF6B7280), fontSize = 13.sp)
                        }
                        "Weekly Points" -> {
                            Text("• Weekly Total: ${NumberFormat.getNumberInstance(Locale.US).format(weeklyPointsTotal)} pts (+6% vs last week)", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Weekly Goal Progress: $weeklyPointsPercent% completed", color = Color(0xFF2E7D32), fontSize = 13.sp)
                            Text("• Highest Activity Day: Friday (Peak performance)", color = Color(0xFF3A86A5), fontSize = 13.sp)
                        }
                        "Distance" -> {
                            Text("• Total Distance: ${currentSnapshot.formattedDistance}", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Steps Logged: ${currentSnapshot.formattedSteps}", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Stride Length: ~0.74 meters", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Active Time" -> {
                            Text("• Total Active Time: ${currentSnapshot.formattedActiveTime}", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Progress: ${currentSnapshot.activeMinutes} of ${currentSnapshot.goalActiveMinutes} mins target", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Oxygen" -> {
                            Text("• Blood Oxygen (SpO2): ${String.format(Locale.US, "%.1f %%", currentSnapshot.oxygenPercent)}", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Status: Optimal respiratory circulation", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Heart Rate" -> {
                            Text("• Current Heart Rate: ${currentSnapshot.heartRateBpm} bpm", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Zone: Aerobic & Active Recovery", color = Color(0xFFFF6E53), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        else -> {
                            Text("• Composite Score: ${currentSnapshot.healthScore}/100", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Status: ${if (currentSnapshot.healthScore >= 75) "Optimal Condition" else "Normal Condition"}", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Custom One UI 7 / FitMax Activity Calendar Modal Bottom Sheet
    if (showCustomCalendarSheet) {
        val initialSelected = try {
            LocalDate.parse(currentSnapshot.fullDateStr)
        } catch (_: Exception) {
            LocalDate.now()
        }
        FitMaxCustomCalendarSheet(
            initialDate = initialSelected,
            onDismiss = { showCustomCalendarSheet = false },
            onDateSelected = { chosenDate ->
                val chosenDateStr = chosenDate.toString()
                val targetIdx = weeklyHistory.indexOfFirst { it.fullDateStr == chosenDateStr }
                if (targetIdx >= 0) {
                    coroutineScope.launch {
                        animIndex.animateTo(
                            targetValue = targetIdx.toFloat(),
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                    selectedDayIndex = targetIdx
                } else {
                    // Chosen date is outside current 45-day window, re-center on it
                    referenceDate = chosenDate
                    selectedDayIndex = HealthMetricsEngine.TODAY_INDEX
                    coroutineScope.launch {
                        animIndex.snapTo(HealthMetricsEngine.TODAY_INDEX.toFloat())
                    }
                }
            }
        )
    }
}

@Composable
private fun ModeTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF111417) else Color(0xFF6B7280)
        )
    }
}

@Composable
private fun SummaryMetricCard(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.6.dp, Color(0xFFE8ECEF)),
        modifier = modifier
            .height(136.dp)
            .m3BounceClick(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF4F6F5), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111417)
                )
            }
        }
    }
}

@Composable
private fun OxygenUpdateCard(
    percentage: String,
    delta: String,
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.6.dp, Color(0xFFE8ECEF)),
        modifier = modifier
            .height(176.dp)
            .m3BounceClick(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Oxygen",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111417)
                    )
                    Text(
                        text = delta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFF2ECC71), CircleShape))
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = percentage,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111417)
                )
            }

            // Dotted soundwave equalizer canvas (matches Image 2 coral pills)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                val coral = Color(0xFFFF6E53)
                val barCount = 7
                val totalW = size.width
                val barW = 6.dp.toPx()
                val spacing = (totalW - (barCount * barW)) / (barCount - 1)

                val heights = listOf(22f, 14f, 30f, 18f, 26f, 34f, 20f)

                heights.forEachIndexed { i, h ->
                    val x = i * (barW + spacing)
                    val y = size.height - h.dp.toPx()

                    drawRoundRect(
                        color = coral,
                        topLeft = Offset(x, y),
                        size = Size(barW, h.dp.toPx()),
                        cornerRadius = CornerRadius(barW / 2, barW / 2)
                    )

                    if (i % 2 == 1) {
                        drawCircle(
                            color = coral,
                            radius = barW / 2,
                            center = Offset(x + barW / 2, y - 6.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeartRateUpdateCard(
    bpm: String,
    delta: String,
    status: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.6.dp, Color(0xFFE8ECEF)),
        modifier = modifier
            .height(176.dp)
            .m3BounceClick(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hart Rate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111417)
                    )
                    Text(
                        text = delta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(5.dp).background(Color(0xFFFF6E53), CircleShape))
                    Text(
                        text = status,
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = bpm,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111417)
                )
            }

            // Pulse waveform / ECG heartbeat wave line canvas (matches Image 2)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                val coral = Color(0xFFFF6E53)
                val w = size.width
                val h = size.height
                val midY = h * 0.65f

                val path = Path().apply {
                    moveTo(0f, midY)
                    lineTo(w * 0.22f, midY)
                    lineTo(w * 0.28f, midY - 6.dp.toPx())
                    lineTo(w * 0.34f, midY)
                    lineTo(w * 0.44f, midY)
                    lineTo(w * 0.48f, midY + 4.dp.toPx())
                    lineTo(w * 0.54f, 4.dp.toPx())
                    lineTo(w * 0.60f, h - 3.dp.toPx())
                    lineTo(w * 0.66f, midY - 4.dp.toPx())
                    lineTo(w * 0.74f, midY)
                    lineTo(w, midY)
                }

                drawPath(
                    path = path,
                    color = coral,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

@Composable
private fun HealthImprovementScoreCard(
    score: String,
    status: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(0.6.dp, Color(0xFFE8ECEF)),
        modifier = Modifier
            .fillMaxWidth()
            .m3BounceClick(onClick = onClick)
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
                Column {
                    Text(
                        text = "Health Improvement Score",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111417)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).background(Color(0xFF2ECC71), CircleShape))
                        Text(
                            text = status,
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = score,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111417)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color(0xFF8A939D),
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Image 2 Adaptation: Workout Tasks Grid (Time 1:30 & Exercises 12/8)
 */
@Composable
fun WorkoutTasksGrid(
    modifier: Modifier = Modifier,
    timeHours: String = "1:30",
    exerciseTasks: String = "12/8",
    onClickTime: () -> Unit = {},
    onClickExercises: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Time Card (Light Blue)
        Card(
            onClick = onClickTime,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FD))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14181B)
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color(0xFFD4EAFC), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Time",
                            tint = Color(0xFF2A85C8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = timeHours,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF14181B)
                )
                Text(
                    text = "Hours",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B8294)
                )
            }
        }

        // Exercises Card (Light Green)
        Card(
            onClick = onClickExercises,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF7E8))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Exercises",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14181B)
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(Color(0xFFD6F2D2), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Exercises",
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = exerciseTasks,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF14181B)
                )
                Text(
                    text = "Tasks",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF658A6A)
                )
            }
        }
    }
}

/**
 * Image 2 Adaptation: Weekly Points Card with 64% Circular Ring Badge and Smooth Line Graph
 */
@Composable
fun WeeklyPointsGraphCard(
    modifier: Modifier = Modifier,
    points: Int = 1544,
    percentage: Int = 64,
    delta: String = "+6%",
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.8.dp, Color(0xFFE8ECEF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: 64% Ring Badge + Weekly points & 1544 score + Three dots menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Circular Progress Ring (64%)
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 5.5.dp.toPx()
                            // Track
                            drawArc(
                                color = Color(0xFFE8ECEF),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            // Progress Ring (64% lime green)
                            drawArc(
                                color = Color(0xFF95BD60),
                                startAngle = -90f,
                                sweepAngle = 360f * (percentage / 100f),
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$percentage%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF14181B)
                        )
                    }

                    Column {
                        Text(
                            text = "Weekly points",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF75828D)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = points.toString(),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF14181B),
                                letterSpacing = (-0.5).sp
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEAF7E8),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropUp,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = delta.replace("+", ""),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = Color(0xFF8C98A4),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Graph Container: Soft light container with dotted guides, smooth curve, peak dot & S M T W T F S
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF7FAFB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val daysCount = 7
                        val stepX = w / (daysCount - 1)

                        // Points for S, M, T, W, T, F, S (Peak at Friday index 5)
                        val yNorm = listOf(0.28f, 0.24f, 0.36f, 0.32f, 0.22f, 0.88f, 0.62f)
                        val coords = yNorm.mapIndexed { idx, p ->
                            Offset(x = idx * stepX, y = h * (1f - p))
                        }

                        // Dotted vertical grid lines
                        coords.forEach { pt ->
                            drawLine(
                                color = Color(0xFFE0E6EA),
                                start = Offset(pt.x, 0f),
                                end = Offset(pt.x, h),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        }

                        // Smooth Cubic Bezier Path
                        val path = Path().apply {
                            moveTo(coords[0].x, coords[0].y)
                            for (i in 0 until coords.size - 1) {
                                val p0 = coords[i]
                                val p1 = coords[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF3A86A5),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Highlight dot on Friday (index 5)
                        val friPt = coords[5]
                        drawCircle(color = Color(0xFF3A86A5), radius = 6.dp.toPx(), center = friPt)
                        drawCircle(color = Color.White, radius = 3.5.dp.toPx(), center = friPt)
                        drawCircle(color = Color(0xFF14181B), radius = 2.dp.toPx(), center = friPt)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Day labels: S M T W T F S
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, day ->
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = if (index == 5) FontWeight.Bold else FontWeight.Medium,
                                color = if (index == 5) Color(0xFF14181B) else Color(0xFF8C96A0),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
