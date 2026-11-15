package com.fixmax.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object SensorLab : Screen("sensor_lab", "Sensor Lab")
    object Recorder : Screen("recorder", "Recorder")
    object HardwareLab : Screen("hardware_lab", "Hardware")
}
