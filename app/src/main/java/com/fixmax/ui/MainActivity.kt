package com.fixmax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.fixmax.feature.dashboard.DashboardScreen
import com.fixmax.feature.hardwarelab.HardwareLabScreen
import com.fixmax.feature.recorder.RecorderScreen
import com.fixmax.feature.sensorlab.SensorLabScreen
import com.fixmax.ui.navigation.Screen
import com.fixmax.ui.theme.FixMaxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FixMaxTheme {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()
    val items = listOf(Screen.Dashboard, Screen.SensorLab, Screen.Recorder, Screen.HardwareLab)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                Screen.Dashboard -> Icon(Icons.Default.DirectionsWalk, contentDescription = null)
                                Screen.SensorLab -> Icon(Icons.Default.Speed, contentDescription = null)
                                Screen.Recorder -> Icon(Icons.Default.FiberManualRecord, contentDescription = null)
                                Screen.HardwareLab -> Icon(Icons.Default.Memory, contentDescription = null)
                            }
                        },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.SensorLab.route) { SensorLabScreen() }
            composable(Screen.Recorder.route) { RecorderScreen() }
            composable(Screen.HardwareLab.route) { HardwareLabScreen() }
        }
    }
}
