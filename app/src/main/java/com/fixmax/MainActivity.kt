package com.fixmax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.fixmax.feature.dashboard.DashboardScreen
import com.fixmax.feature.hardwarelab.HardwareLabScreen
import com.fixmax.feature.recorder.RecorderScreen
import com.fixmax.feature.sensorlab.SensorLabScreen
import com.fixmax.tracking.StepTrackingService
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
    val context = LocalContext.current
    val navController = rememberNavController()
    val items = listOf(Screen.Dashboard, Screen.SensorLab, Screen.Recorder, Screen.HardwareLab)

    // Check runtime permissions
    var hasActivityPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasActivityPermission = results[Manifest.permission.ACTIVITY_RECOGNITION] ?: hasActivityPermission
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = results[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotificationPermission
        }
        if (hasActivityPermission) {
            StepTrackingService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        val toRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasActivityPermission) {
            toRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            toRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (toRequest.isNotEmpty()) {
            permissionLauncher.launch(toRequest.toTypedArray())
        } else {
            StepTrackingService.start(context)
        }
    }

    Scaffold(
        topBar = {
            if (!hasActivityPermission || !hasNotificationPermission) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Motion permissions required for background tracking",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val toRequest = mutableListOf<String>()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasActivityPermission) {
                                    toRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    toRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionLauncher.launch(toRequest.toTypedArray())
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Grant", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
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
