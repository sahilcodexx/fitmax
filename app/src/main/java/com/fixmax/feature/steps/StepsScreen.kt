package com.fixmax.feature.steps

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
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Place
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.tracking.StepTrackingService
import com.fixmax.ui.components.m3BounceClick
import kotlinx.coroutines.launch
import java.text.NumberFormat
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
    val baseSteps = if (liveSteps > 0) liveSteps.toInt() else 7842

    val dateList = remember {
        listOf(
            Triple("Sat", "8", 6200),
            Triple("Sun", "9", 6500),
            Triple("Mon", "10", 7100),
            Triple("Tue", "11", 6850),
            Triple("Wed", "12", baseSteps), // Active Day (Matches Image 1)
            Triple("Thu", "13", 7420),
            Triple("Fri", "14", 8200),
            Triple("Sat", "15", 9400),
            Triple("Sun", "16", 7900),
            Triple("Mon", "17", 8300)
        )
    }

    var selectedDayIndex by remember { mutableStateOf(4) } // Index 4 is Wed 12
    var selectedMode by remember { mutableStateOf("Activity") } // "Nutrition" or "Activity"
    var showMetricDialog by remember { mutableStateOf<String?>(null) }

    val currentDayData = dateList[selectedDayIndex.coerceIn(dateList.indices)]

    // Target values for animated counter
    val targetNumber = if (selectedMode == "Nutrition") 1345 else currentDayData.third

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
    // Animatable float index for buttery smooth drag & snap (starts at 4f: Wed 12)
    val animIndex = remember { Animatable(4f) }
    val density = LocalDensity.current
    val itemSpacingPx = remember(density) { with(density) { 74.dp.toPx() } }
    val itemWidthPx = remember(density) { with(density) { 60.dp.toPx() } }
    val maxDipPx = remember(density) { with(density) { 36.dp.toPx() } }

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
                value = if (selectedMode == "Activity") "5.8 km" else "6.7 km",
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
                value = "1h 12m",
                onClick = { showMetricDialog = "Active Time" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Section Header: "Daily Updates"
        Text(
            text = "Daily Updates",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111417)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. 2-Column Daily Updates Cards (Oxygen & Hart Rate)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Oxygen Card
            OxygenUpdateCard(
                percentage = "84,3 %",
                delta = "+2%",
                status = "Normal",
                onClick = { showMetricDialog = "Oxygen" },
                modifier = Modifier.weight(1f)
            )

            // Hart Rate Card
            HeartRateUpdateCard(
                bpm = "102 bpm",
                delta = "-7%",
                status = "Low",
                onClick = { showMetricDialog = "Heart Rate" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Health Improvement Score Card (54%, Normal)
        HealthImprovementScoreCard(
            score = "54%",
            status = "Normal",
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
                        "Distance" -> {
                            Text("• Measured Stride: 0.74 meters", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Average Pace: 10'48\" per km", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Dynamic Sensor Estimation: Active", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Active Time" -> {
                            Text("• Brisk Walking: 52 minutes", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Moderate Stride: 20 minutes", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Target Milestone: 60 mins (Exceeded!)", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Oxygen" -> {
                            Text("• Blood Oxygen (SpO2): 84.3%", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Variance: +2% vs 7-day baseline", color = Color(0xFF005A43), fontSize = 13.sp)
                            Text("• Status: Normal respiratory rate", color = Color(0xFF6B7280), fontSize = 13.sp)
                        }
                        "Heart Rate" -> {
                            Text("• Current Heart Rate: 102 bpm", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Resting Rate: 64 bpm", color = Color(0xFF111417), fontSize = 14.sp)
                            Text("• Training Zone: Fat Burn & Recovery", color = Color(0xFFFF6E53), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        else -> {
                            Text("• Composite Score: 54/100", color = Color(0xFF111417), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Factor 1: Regular step consistency", color = Color(0xFF111417), fontSize = 13.sp)
                            Text("• Factor 2: Steady cardiovascular workload", color = Color(0xFF005A43), fontSize = 13.sp)
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
