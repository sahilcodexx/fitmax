package com.fixmax.feature.hardwarelab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
        Text("Hardware Capability Inspector", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(report.deviceModel, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Discovered Motion Sensors:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                HorizontalDivider()

                SensorItem(
                    name = "Accelerometer (3-Axis)",
                    detail = report.accelName,
                    vendor = report.accelVendor,
                    status = "AVAILABLE (50 Hz Target)"
                )

                SensorItem(
                    name = "Gyroscope (Rotational)",
                    detail = report.gyroName,
                    vendor = report.gyroVendor,
                    status = "AVAILABLE"
                )

                SensorItem(
                    name = "Hardware Step Counter",
                    detail = report.stepCounterName,
                    vendor = "Samsung sensor_hub",
                    status = if (report.hasHardwareStepCounter) "HARDWARE READY" else "NOT PRESENT"
                )

                SensorItem(
                    name = "Hardware Step Detector",
                    detail = "TYPE_STEP_DETECTOR",
                    vendor = "Samsung sensor_hub",
                    status = if (report.hasHardwareStepDetector) "AVAILABLE (<2s latency)" else "NOT PRESENT"
                )

                SensorItem(
                    name = "Linear Acceleration",
                    detail = "TYPE_LINEAR_ACCELERATION",
                    vendor = "Sensor Fusion",
                    status = if (report.hasLinearAcceleration) "AVAILABLE" else "NOT PRESENT"
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recommended Engine:", fontWeight = FontWeight.Bold)
                    Text(
                        text = report.recommendedTrackingMode.name,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorItem(name: String, detail: String, vendor: String, status: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(status, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
        Text("Device: $detail ($vendor)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
