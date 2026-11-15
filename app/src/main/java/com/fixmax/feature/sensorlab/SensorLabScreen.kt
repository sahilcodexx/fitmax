package com.fixmax.feature.sensorlab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
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
import com.fixmax.core.processing.GravityFilter
import kotlin.math.sqrt

@Composable
fun SensorLabScreen() {
    val context = LocalContext.current

    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(0f) }
    var accelMag by remember { mutableFloatStateOf(0f) }
    var dynamicMag by remember { mutableFloatStateOf(0f) }

    var gyroX by remember { mutableFloatStateOf(0f) }
    var gyroY by remember { mutableFloatStateOf(0f) }
    var gyroZ by remember { mutableFloatStateOf(0f) }
    var gyroMag by remember { mutableFloatStateOf(0f) }

    val gravityFilter = remember { GravityFilter(cutoffHz = 0.30f, samplingRateHz = 50.0f) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val thread = HandlerThread("SensorLabThread").apply { start() }
        val handler = Handler(thread.looper)

        var lastUiUpdate = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val now = System.currentTimeMillis()

                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val mag = sqrt(x * x + y * y + z * z)
                    val dyn = gravityFilter.filter(mag, event.timestamp)

                    // Throttle UI update to ~25 FPS to keep Jetpack Compose butter smooth
                    if (now - lastUiUpdate > 40) {
                        lastUiUpdate = now
                        accelX = x
                        accelY = y
                        accelZ = z
                        accelMag = mag
                        dynamicMag = dyn
                    }
                } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val mag = sqrt(x * x + y * y + z * z)

                    if (now - lastUiUpdate > 40) {
                        gyroX = x
                        gyroY = y
                        gyroZ = z
                        gyroMag = mag
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (accel != null) sm.registerListener(listener, accel, 20_000, handler)
        if (gyro != null) sm.registerListener(listener, gyro, 20_000, handler)

        onDispose {
            sm.unregisterListener(listener)
            thread.quitSafely()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Live Sensor Lab (50 Hz)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Real-Time Motion Signals • ICM42632M IMU", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        // Accelerometer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Accelerometer (Raw 3-Axis)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("X (lateral):", fontWeight = FontWeight.Medium)
                    Text(String.format("%+.3f m/s²", accelX), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Y (longitudinal):", fontWeight = FontWeight.Medium)
                    Text(String.format("%+.3f m/s²", accelY), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Z (vertical):", fontWeight = FontWeight.Medium)
                    Text(String.format("%+.3f m/s²", accelZ), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Magnitude ||a||:", fontWeight = FontWeight.Bold)
                    Text(String.format("%.3f m/s²", accelMag), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dynamic Movement (Gravity Removed):", fontWeight = FontWeight.Bold)
                    Text(String.format("%+.3f m/s²", dynamicMag), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        // Gyroscope Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Gyroscope (Rotational Velocity)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, fontSize = 16.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Roll (X):", fontWeight = FontWeight.Medium)
                    Text(String.format("%+.3f rad/s", gyroX), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pitch (Y):", fontWeight = FontWeight.Medium)
                    Text(String.format("%+.3f rad/s", gyroY), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Yaw (Z):", fontWeight = FontWeight.Medium)
                    Text(String.format("%+.3f rad/s", gyroZ), fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Angular Velocity ||ω||:", fontWeight = FontWeight.Bold)
                    Text(String.format("%.3f rad/s", gyroMag), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}
