package com.fixmax.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ChevronRight
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
import com.fixmax.data.repository.HealthMetricsEngine
import com.fixmax.tracking.StepTrackingService
import com.fixmax.ui.components.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSteps: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val liveSteps by StepTrackingService.liveStepsToday.collectAsState()
    val displaySteps = if (liveSteps > 0) liveSteps else 1947L

    // Rolling Step Number Counter Animation (0 -> displaySteps)
    var startCounting by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startCounting = true
    }
    val animatedSteps by animateIntAsState(
        targetValue = if (startCounting) displaySteps.toInt() else 0,
        animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
        label = "stepsCountRoll"
    )

    // Dynamic Real Health Math Engine
    val todaySnapshot = remember(animatedSteps) {
        HealthMetricsEngine.computeSnapshot(animatedSteps.toLong())
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

        // Top Header: Profile Avatar ("Hello, Sophia" -> opens Profile) + Notifications
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .m3BounceClick(onClick = onNavigateToProfile)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNavigateToProfile
                    )
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFFFD7C2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👩‍🦰", fontSize = 24.sp)
                }
                Text(
                    text = "Hello, Sophia",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828)
                )
            }

            // Notification Bell Button with unclipped badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .m3BounceClick {
                        showNotificationSheet = true
                        hasUnreadNotifications = false
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            showNotificationSheet = true
                            hasUnreadNotifications = false
                        }
                    )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = BorderStroke(0.8.dp, Color(0xFFE8ECEF))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF1A2E26),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 3.dp, end = 3.dp)
                            .size(10.dp)
                            .graphicsLayer {
                                scaleX = notifDotScale
                                scaleY = notifDotScale
                            }
                            .background(Color(0xFFFF3B30), CircleShape)
                            .border(1.5.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Samsung Health Orange Active Workout Hero Card (Image 1 top highlight) - Dynamic Math
        SamsungWorkoutHeroCard(
            distanceKm = String.format(Locale.US, "%.2f", todaySnapshot.distanceKm),
            activityType = "Walking",
            duration = todaySnapshot.formattedActiveTime,
            steps = todaySnapshot.steps.toInt(),
            caloriesBurned = todaySnapshot.caloriesBurned,
            onClick = onNavigateToActivity
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Samsung Daily Activity Card (3-Layer Concentric Circle Bar + Live Steps, Active Time, Calories) - Dynamic Math
        SamsungDailyActivityCard(
            steps = todaySnapshot.steps.toInt(),
            activeMinutes = todaySnapshot.activeMinutes,
            caloriesBurned = todaySnapshot.caloriesBurned,
            onClick = { showDailyActivitySheet = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Samsung Vivid Green Step Goal Progress Card - Dynamic Math
        SamsungGreenStepGoalCard(
            currentSteps = todaySnapshot.steps.toInt(),
            goalSteps = todaySnapshot.goalSteps.toInt(),
            onClick = onNavigateToSteps
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Samsung One UI 7 2-Column Grid Cards: Vitals & Cardio Load - Dynamic Math
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
                sleepScore = todaySnapshot.sleepScore,
                subtitle = "Track your sleep to measure vitals.",
                onClick = { selectedStatDetail = "Vitals" },
                modifier = Modifier.weight(1f)
            )
            SamsungCardioLoadCard(
                status = todaySnapshot.cardioLoad,
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

        // Quick Stats 2x2 Grid (Rich One UI 7 Health Cards) - Dynamic Math
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SamsungCaloriesCard(
                calories = todaySnapshot.caloriesBurned.toString(),
                subtitle = "Active burn on track",
                onClick = { selectedStatDetail = "Calories" },
                modifier = Modifier.weight(1f)
            )
            SamsungActiveTimeCard(
                minutes = todaySnapshot.activeMinutes.toString(),
                subtitle = "${(60 - todaySnapshot.activeMinutes).coerceAtLeast(0)}m left to daily goal",
                onClick = { selectedStatDetail = "Active Time" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SamsungDistanceCard(
                distance = String.format(Locale.US, "%.1f", todaySnapshot.distanceKm),
                subtitle = "${todaySnapshot.formattedSteps} steps covered",
                onClick = { selectedStatDetail = "Distance" },
                modifier = Modifier.weight(1f)
            )
            SamsungStairsCard(
                floors = todaySnapshot.floors.toString(),
                subtitle = "Elevation ~${todaySnapshot.floors * 3} meters",
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

        Spacer(modifier = Modifier.height(26.dp))

        // FitMax Branding Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFDCF4E9),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = "FitMax",
                        tint = Color(0xFF005A43),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "FitMax",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF005A43),
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Smart Health & Daily Activity Tracker",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B867B)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Designed for Samsung Galaxy • One UI 7",
                fontSize = 11.sp,
                color = Color(0xFF94A8A0)
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
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
                        Text("Done", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                NotificationItem("🎯 Milestone Reached", "You've crushed today's step goal! Keep going strong.", "10m ago")
                NotificationItem("💧 Hydration Reminder", "Time to drink a glass of water to keep your body optimized.", "45m ago")
                NotificationItem("🏃 Quick Cardio", "Sophia completed a 5.8 km run. Tap to congratulate!", "2h ago")
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Samsung Daily Activity Detail Modal Sheet
    if (showDailyActivitySheet) {
        SamsungDailyActivityDetailSheet(
            onDismiss = { showDailyActivitySheet = false },
            steps = todaySnapshot.steps.toInt(),
            activeMinutes = todaySnapshot.activeMinutes,
            caloriesBurned = todaySnapshot.caloriesBurned,
            stepsGoal = todaySnapshot.goalSteps.toInt(),
            activeMinutesGoal = todaySnapshot.goalActiveMinutes,
            caloriesGoal = todaySnapshot.goalCalories
        )
    }

    // Quick Stats Detail Dialog
    selectedStatDetail?.let { statName ->
        AlertDialog(
            onDismissRequest = { selectedStatDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedStatDetail = null }) {
                    Text("Got it", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(text = "$statName Insights", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
            },
            text = {
                Text(
                    text = when (statName) {
                        "Calories" -> "You've burned ${todaySnapshot.caloriesBurned} kcal actively today. Metabolic efficiency is steady."
                        "Active Time" -> "You have logged ${todaySnapshot.activeMinutes} minutes of brisk activity today. ${todaySnapshot.goalActiveMinutes - todaySnapshot.activeMinutes}m remaining to reach your daily 60m target."
                        "Distance" -> "You've walked and run a total of ${todaySnapshot.formattedDistance} across ${todaySnapshot.formattedSteps} steps."
                        "Stairs" -> "You have climbed ${todaySnapshot.floors} floors (approximately ${todaySnapshot.floors * 3} vertical meters)."
                        "Vitals" -> "Sleep quality score is ${todaySnapshot.sleepScore}. Resting heart rate averages ${todaySnapshot.heartRateBpm} bpm."
                        "Cardio Load" -> "Cardio load status is currently ${todaySnapshot.cardioLoad} based on your training cadence."
                        else -> "Keep staying active to maintain peak physical and cognitive condition."
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF4A5568)
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Streak Celebratory Dialog
    if (showStreakDialog) {
        AlertDialog(
            onDismissRequest = { showStreakDialog = false },
            confirmButton = {
                TextButton(onClick = { showStreakDialog = false }) {
                    Text("Awesome!", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏆", fontSize = 24.sp)
                    Text("3-Day Streak!", fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
                }
            },
            text = {
                Text(
                    text = "You've reached your daily movement goal 3 days in a row! Maintain momentum to unlock the 7-day Master Streak badge.",
                    fontSize = 14.sp,
                    color = Color(0xFF4A5568)
                )
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun NotificationItem(title: String, desc: String, time: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 12.sp, color = Color(0xFF6B867B), lineHeight = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = time, fontSize = 10.sp, color = Color(0xFF9EBAAE))
    }
}
