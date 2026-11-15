package com.fixmax.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.tracking.StepTrackingService
import com.fixmax.ui.components.SamsungActiveTimeCard
import com.fixmax.ui.components.SamsungActivityTimeline
import com.fixmax.ui.components.SamsungCaloriesCard
import com.fixmax.ui.components.SamsungCardioLoadCard
import com.fixmax.ui.components.SamsungDailyActivityCard
import com.fixmax.ui.components.SamsungDailyActivityDetailSheet
import com.fixmax.ui.components.SamsungDistanceCard
import com.fixmax.ui.components.SamsungExerciseBar
import com.fixmax.ui.components.SamsungGreenStepGoalCard
import com.fixmax.ui.components.SamsungStairsCard
import com.fixmax.ui.components.SamsungVitalsCard
import com.fixmax.ui.components.SamsungWorkoutHeroCard
import com.fixmax.ui.components.m3BounceClick
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSteps: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {}
) {
    val liveSteps by StepTrackingService.liveStepsToday.collectAsState()
    val displaySteps = if (liveSteps > 0) liveSteps else 7842L

    // Rolling Step Number Counter Animation (0 -> 7,842)
    var startCounting by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startCounting = true
    }
    val animatedSteps by animateIntAsState(
        targetValue = if (startCounting) displaySteps.toInt() else 0,
        animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
        label = "stepsCountRoll"
    )
    val formattedSteps = remember(animatedSteps) {
        NumberFormat.getNumberInstance(Locale.US).format(animatedSteps)
    }

    var hasUnreadNotifications by remember { mutableStateOf(true) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showDailyActivitySheet by remember { mutableStateOf(false) }
    var selectedStatDetail by remember { mutableStateOf<String?>(null) }
    var showStreakDialog by remember { mutableStateOf(false) }

    // Ambient Infinite Transitions
    val infiniteTransition = rememberInfiniteTransition(label = "homeAmbient")

    // Pulsing notification dot
    val notifDotScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "notifDotScale"
    )

    // Trophy celebratory tilt
    val trophyTilt by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "trophyTilt"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FCF9))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // FitMax Header + Notification Bell with Animated Orange Pulse
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FitMax",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF005A43)
            )

            Box {
                IconButton(
                    onClick = {
                        showNotificationSheet = true
                        hasUnreadNotifications = false
                    },
                    modifier = Modifier.m3BounceClick {
                        showNotificationSheet = true
                        hasUnreadNotifications = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF1A2E26)
                    )
                }
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .graphicsLayer {
                                scaleX = notifDotScale
                                scaleY = notifDotScale
                            }
                            .background(Color(0xFFFF6D00), CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Samsung Health Orange Active Workout Hero Card (Image 1 top highlight)
        SamsungWorkoutHeroCard(
            distanceKm = "7.96",
            activityType = "Walking",
            duration = "1:41:05",
            steps = 10416,
            caloriesBurned = 568,
            onClick = onNavigateToActivity
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Samsung Daily Activity Card (3-Layer Concentric Circle Bar + Live Steps, Active Time, Calories)
        SamsungDailyActivityCard(
            steps = animatedSteps,
            activeMinutes = 54,
            caloriesBurned = 320,
            onClick = { showDailyActivitySheet = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Samsung Vivid Green Step Goal Progress Card (Image 1 bottom highlight)
        SamsungGreenStepGoalCard(
            currentSteps = if (animatedSteps > 0) animatedSteps else 10980,
            goalSteps = 10000,
            onClick = onNavigateToSteps
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Exercise Quick Launch Bar (Walk, Run, Cycle, Log)
        SamsungExerciseBar(
            onWalkClick = onNavigateToSteps,
            onRunClick = onNavigateToActivity,
            onCycleClick = onNavigateToActivity,
            onLogClick = onNavigateToActivity
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Samsung One UI 7 2-Column Grid Cards: Vitals & Cardio Load (Image 2 highlight)
        Text(
            text = "Health & Vitals",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A2E26)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SamsungVitalsCard(
                sleepScore = "94 Good",
                subtitle = "Track your sleep to measure vitals.",
                onClick = { selectedStatDetail = "Vitals" },
                modifier = Modifier.weight(1f)
            )
            SamsungCardioLoadCard(
                status = "Optimal",
                subtitle = "Find your daily training sweet spot.",
                onClick = { selectedStatDetail = "Cardio Load" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Stats Header + "See all →" Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quick Stats",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A2E26)
            )
            TextButton(
                onClick = onNavigateToSteps,
                modifier = Modifier.m3BounceClick { onNavigateToSteps() },
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "See all →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF005A43)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Stats 2x2 Grid (Rich One UI 7 Health Cards)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SamsungCaloriesCard(
                calories = "320",
                subtitle = "Active burn on track",
                onClick = { selectedStatDetail = "Calories" },
                modifier = Modifier.weight(1f)
            )
            SamsungActiveTimeCard(
                minutes = "54",
                subtitle = "6m left to daily goal",
                onClick = { selectedStatDetail = "Active Time" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SamsungDistanceCard(
                distance = "5.8",
                subtitle = "7,842 steps covered",
                onClick = { selectedStatDetail = "Distance" },
                modifier = Modifier.weight(1f)
            )
            SamsungStairsCard(
                floors = "12",
                subtitle = "Elevation ~36 meters",
                onClick = { selectedStatDetail = "Stairs" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Streak Banner Card with Celebratory Animated Trophy
        Card(
            onClick = { showStreakDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .m3BounceClick { showStreakDialog = true },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCF4E9))
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
                        modifier = Modifier.graphicsLayer {
                            rotationZ = trophyTilt
                        }
                    ) {
                        Text(text = "🏆", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "You're on a roll!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A2E26)
                        )
                        Text(
                            text = "Hit your daily goal for 3 days in a row.",
                            fontSize = 12.sp,
                            color = Color(0xFF6B867B)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = Color(0xFF005A43)
                )
            }
        }

        Spacer(modifier = Modifier.height(130.dp))
    }

    // Notifications Bottom Sheet
    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A2E26)
                    )
                    TextButton(onClick = { showNotificationSheet = false }) {
                        Text("Clear All", color = Color(0xFF005A43), fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                NotificationItem(
                    title = "🎉 Daily Goal Accomplished!",
                    desc = "You crushed your 10,000 steps goal yesterday with 10,420 steps.",
                    time = "Yesterday"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F5F2))
                NotificationItem(
                    title = "🔥 3-Day Walking Streak",
                    desc = "You have stayed active 3 days in a row. Keep the rhythm going!",
                    time = "Today 08:30 AM"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F5F2))
                NotificationItem(
                    title = "⚡ Samsung Sensor Hub Active",
                    desc = "Hardware pedometer is actively tracking steps in the background.",
                    time = "Active now"
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Quick Stat Detail Dialog
    selectedStatDetail?.let { statName ->
        AlertDialog(
            onDismissRequest = { selectedStatDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedStatDetail = null }) {
                    Text("Close", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(text = "$statName Breakdown", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (statName) {
                        "Vitals" -> {
                            Text("• Sleep Score: 94 Good", color = Color(0xFF1A2E26), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• Resting Heart Rate: 62 bpm (Optimal)", color = Color(0xFF0097A7), fontSize = 13.sp)
                            Text("• Blood Oxygen (SpO2): 98%", color = Color(0xFF1A2E26), fontSize = 13.sp)
                            Text("• Skin Temperature: Normal (+0.1°C)", color = Color(0xFF6B867B), fontSize = 13.sp)
                        }
                        "Cardio Load" -> {
                            Text("• Training Sweet Spot: Optimal", color = Color(0xFF1A2E26), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("• 7-Day Training Load: 420 (Balanced)", color = Color(0xFF1E88E5), fontSize = 13.sp)
                            Text("• Cardio Recovery Remaining: 14 hrs", color = Color(0xFF1A2E26), fontSize = 13.sp)
                            Text("• Risk Level: Minimal injury risk", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Calories" -> {
                            Text("• Active Burn: 245 kcal", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Basal Metabolic Rate: 75 kcal", color = Color(0xFF6B867B), fontSize = 13.sp)
                            Text("• Daily Calorie Goal: 450 kcal (71% reached)", color = Color(0xFF005A43), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        "Active Time" -> {
                            Text("• Brisk Walking: 38 min", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Light Activity: 16 min", color = Color(0xFF6B867B), fontSize = 13.sp)
                            Text("• Target: 60 minutes per day", color = Color(0xFF005A43), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        "Distance" -> {
                            Text("• Step Stride Length: 0.74 m", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Measured Distance: 5.8 km", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• High precision GPS-free sensor estimation", color = Color(0xFF005A43), fontSize = 13.sp)
                        }
                        "Stairs" -> {
                            Text("• Floors Climbed: 12 floors", color = Color(0xFF1A2E26), fontSize = 14.sp)
                            Text("• Elevation Gain: ~36 meters", color = Color(0xFF6B867B), fontSize = 13.sp)
                        }
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Streak Celebration Dialog
    if (showStreakDialog) {
        AlertDialog(
            onDismissRequest = { showStreakDialog = false },
            confirmButton = {
                Button(
                    onClick = { showStreakDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005A43)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Keep Moving!")
                }
            },
            title = {
                Text(text = "🏆 3-Day Walking Streak!", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Column {
                    Text(
                        text = "Great dedication, Sahil! You have achieved your daily step goal 3 days in a row.",
                        color = Color(0xFF1A2E26),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Next milestone: 7-day Bronze Medalist Badge (4 days to go).",
                        color = Color(0xFF005A43),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Samsung Daily Activity Detail Sheet (Full Heart Ribbon + Weekday Strip + 24h Timeline)
    if (showDailyActivitySheet) {
        SamsungDailyActivityDetailSheet(
            onDismiss = { showDailyActivitySheet = false },
            steps = animatedSteps,
            activeMinutes = 54,
            caloriesBurned = 320
        )
    }
}

@Composable
private fun NotificationItem(title: String, desc: String, time: String) {
    Column {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 12.sp, color = Color(0xFF6B867B), lineHeight = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = time, fontSize = 10.sp, color = Color(0xFF9EBAAE))
    }
}
