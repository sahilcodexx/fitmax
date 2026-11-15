package com.fixmax.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import com.fixmax.feature.activity.ActivityScreen
import com.fixmax.feature.home.HomeScreen
import com.fixmax.feature.profile.ProfileScreen
import com.fixmax.feature.steps.StepsScreen
import com.fixmax.ui.components.m3BounceClick

sealed class NavTab(val route: String, val title: String) {
    object Home : NavTab("home", "Home")
    object Stats : NavTab("stats", "Together")
    object Workouts : NavTab("workouts", "Workouts")
    object Profile : NavTab("profile", "Profile")
}

@Composable
fun MainFigmaScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showQuickActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF7FCF9),
        bottomBar = {
            // Floating Island Navigation Bar + Companion Action Button (One UI 7 / Samsung Health)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Main Floating Capsule Island
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp),
                        shape = RoundedCornerShape(31.dp),
                        color = Color.White,
                        shadowElevation = 12.dp,
                        border = BorderStroke(0.6.dp, Color(0xFFE4EBE6))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(NavTab.Home, NavTab.Stats, NavTab.Workouts, NavTab.Profile).forEach { tab ->
                                val isSelected = currentRoute == tab.route ||
                                        (tab == NavTab.Stats && (currentRoute == "steps_detail" || currentRoute == "activity_detail"))

                                val (icon, selectedIcon) = when (tab) {
                                    NavTab.Home -> Icons.Outlined.Home to Icons.Filled.Home
                                    NavTab.Stats -> Icons.Outlined.Flag to Icons.Filled.Flag
                                    NavTab.Workouts -> Icons.Outlined.FitnessCenter to Icons.Filled.FitnessCenter
                                    NavTab.Profile -> Icons.Outlined.Person to Icons.Filled.Person
                                }

                                ExpressiveNavTabItem(
                                    selected = isSelected,
                                    icon = icon,
                                    selectedIcon = selectedIcon,
                                    label = tab.title,
                                    onClick = {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Companion Floating Action Button on the right (matches Image 2 [ + ] scanner/action)
                    Surface(
                        modifier = Modifier
                            .size(58.dp)
                            .m3BounceClick {
                                showQuickActionSheet = true
                            },
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 12.dp,
                        border = BorderStroke(0.6.dp, Color(0xFFE4EBE6))
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CropFree,
                                contentDescription = "Quick Workout Actions",
                                tint = Color(0xFF005A43),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Screen Content (Reserved padding avoids text slicing or UI errors behind bottom bar)
        NavHost(
            navController = navController,
            startDestination = NavTab.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            enterTransition = {
                fadeIn(animationSpec = tween(280)) + slideInHorizontally(
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) { it / 5 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(220)) + slideOutHorizontally(
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) { -it / 5 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(280)) + slideInHorizontally(
                    animationSpec = tween(280, easing = FastOutSlowInEasing)
                ) { -it / 5 }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(220)) + slideOutHorizontally(
                    animationSpec = tween(220, easing = FastOutSlowInEasing)
                ) { it / 5 }
            }
        ) {
            composable(NavTab.Home.route) {
                HomeScreen(
                    onNavigateToSteps = {
                        navController.navigate(NavTab.Stats.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToActivity = {
                        navController.navigate(NavTab.Workouts.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(NavTab.Stats.route) {
                StepsScreen(onBack = {
                    navController.navigate(NavTab.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable("steps_detail") {
                StepsScreen(onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(NavTab.Home.route)
                    }
                })
            }
            composable(NavTab.Workouts.route) {
                ActivityScreen(onBack = {
                    navController.navigate(NavTab.Home.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
            composable(NavTab.Profile.route) {
                ProfileScreen()
            }
        }

        // Quick Actions Modal Bottom Sheet
        if (showQuickActionSheet) {
            QuickActionBottomSheet(
                onDismiss = { showQuickActionSheet = false },
                onStartWorkout = {
                    navController.navigate(NavTab.Workouts.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToStats = {
                    navController.navigate(NavTab.Stats.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * Material 3 Expressive Animated Navigation Tab Item.
 * Clean icon + label (no background box, matching user request and One UI 7).
 */
@Composable
private fun RowScope.ExpressiveNavTabItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navIconScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF005A43) else Color(0xFF8FA79D),
        animationSpec = tween(durationMillis = 180),
        label = "navIconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF005A43) else Color(0xFF8FA79D),
        animationSpec = tween(durationMillis = 180),
        label = "navTextColor"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    } catch (_: Exception) {}
                    onClick()
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp, 26.dp)
        ) {
            // Clean animated scaling icon with NO background box
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionBottomSheet(
    onDismiss: () -> Unit,
    onStartWorkout: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A2E26)
                )
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0xFF005A43), fontWeight = FontWeight.Bold)
                }
            }

            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                iconBg = Color(0xFFFFF0E6),
                iconTint = Color(0xFFFF6D00),
                title = "Start Walking Workout",
                subtitle = "Track GPS route, cadence, and calories burned",
                onClick = {
                    onDismiss()
                    onStartWorkout()
                }
            )

            QuickActionButton(
                icon = Icons.Default.Favorite,
                iconBg = Color(0xFFE0F7FA),
                iconTint = Color(0xFF0097A7),
                title = "Record Vitals & Recovery",
                subtitle = "Check resting heart rate and sleep balance",
                onClick = {
                    onDismiss()
                    onNavigateToStats()
                }
            )

            QuickActionButton(
                icon = Icons.Default.QrCodeScanner,
                iconBg = Color(0xFFE8F5E9),
                iconTint = Color(0xFF005A43),
                title = "Samsung Sensor Hub Active",
                subtitle = "Hardware pedometer actively measuring steps",
                onClick = onDismiss
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF7FCF9),
        modifier = Modifier
            .fillMaxWidth()
            .m3BounceClick(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A2E26))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = Color(0xFF6B867B))
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8FA79D))
        }
    }
}
