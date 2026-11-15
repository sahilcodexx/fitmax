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

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        bottomBar = {
            // Unified Floating Island Capsule Navigation Bar (matches Image 1 without separate button)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = Color.White,
                    shadowElevation = 14.dp,
                    tonalElevation = 2.dp,
                    border = BorderStroke(0.6.dp, Color(0xFFE4EBE6))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(NavTab.Home, NavTab.Stats, NavTab.Workouts, NavTab.Profile).forEach { tab ->
                            val isSelected = currentRoute == tab.route ||
                                    (tab == NavTab.Stats && (currentRoute == "steps_detail" || currentRoute == "activity_detail"))

                            val (icon, selectedIcon) = when (tab) {
                                NavTab.Home -> Icons.Outlined.Home to Icons.Filled.Home
                                NavTab.Stats -> Icons.Outlined.Flag to Icons.Filled.Flag
                                NavTab.Workouts -> Icons.Outlined.CalendarToday to Icons.Filled.CalendarToday
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
            }
        }
    ) { paddingValues ->
        // Screen Content with reserved bottom padding
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
    }
}

/**
 * Material 3 Expressive Animated Navigation Tab Item.
 * Matches Image 1: Active tab is enclosed in a soft gray capsule pill wrapping icon and text.
 */
@Composable
private fun ExpressiveNavTabItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navIconScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF14181F) else Color(0xFF6B7280),
        animationSpec = tween(durationMillis = 180),
        label = "navIconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF14181F) else Color(0xFF6B7280),
        animationSpec = tween(durationMillis = 180),
        label = "navTextColor"
    )

    Box(
        modifier = Modifier
            .background(
                color = if (selected) Color(0xFFE2E6E9) else Color.Transparent,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = if (selected) 14.dp else 10.dp, vertical = 6.dp)
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
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
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
}
