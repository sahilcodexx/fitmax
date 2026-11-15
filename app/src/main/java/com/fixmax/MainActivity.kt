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
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.*
import com.fixmax.feature.dashboard.WellnessDashboardScreen
import com.fixmax.feature.hardwarelab.HardwareLabScreen
import com.fixmax.feature.recorder.RecorderScreen
import com.fixmax.feature.sensorlab.SensorLabScreen
import com.fixmax.tracking.StepTrackingService
import com.fixmax.ui.navigation.Screen
import com.fixmax.ui.theme.FixMaxWellnessTheme
import com.fixmax.ui.theme.MintBackground

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(245, 251, 248)
        window.navigationBarColor = android.graphics.Color.rgb(245, 251, 248)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContent {
            FixMaxWellnessTheme {
                MainAppScaffold()
            }
        }
    }
}

@Composable
fun MainAppScaffold() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var showDevTabs by remember { mutableStateOf(false) }
    var showIntro by remember { mutableStateOf(!context.getSharedPreferences("fitmax", 0).getBoolean("intro_seen", false)) }

    // Runtime permissions handling
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

    LaunchedEffect(showIntro) {
        if (showIntro) return@LaunchedEffect
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

    if (showIntro) {
        com.fixmax.feature.onboarding.IntroScreen {
            context.getSharedPreferences("fitmax", 0).edit().putBoolean("intro_seen", true).apply()
            showIntro = false
        }
        return
    }

    com.fixmax.ui.navigation.MainFigmaScaffold()
}
