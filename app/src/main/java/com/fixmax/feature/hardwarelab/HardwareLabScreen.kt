package com.fixmax.feature.hardwarelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fixmax.core.sensors.SensorDiscovery

@Composable
fun HardwareLabScreen() {
    val context = LocalContext.current
    val discovery = remember { SensorDiscovery(context) }
    val report = remember { discovery.inspect() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Hardware Capability Inspector", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(report.deviceModel, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sensors Discovered:", fontWeight = FontWeight.Bold)
                Text("• Accelerometer: ${report.accelName} (${report.accelVendor})")
                Text("• Gyroscope: ${report.gyroName} (${report.gyroVendor})")
                Text("• Hardware Step Counter: ${report.stepCounterName} -> ${if (report.hasHardwareStepCounter) "AVAILABLE" else "NOT PRESENT"}")
                Text("• Hardware Step Detector: ${if (report.hasHardwareStepDetector) "AVAILABLE" else "NOT PRESENT"}")
                Text("• Linear Acceleration: ${if (report.hasLinearAcceleration) "AVAILABLE" else "NOT PRESENT"}")
                HorizontalDivider()
                Text("Recommended Mode: ${report.recommendedTrackingMode}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
