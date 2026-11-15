package com.fixmax.feature.sensorlab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

@Composable
fun SensorLabScreen() {
    val context = LocalContext.current
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }
    var accelZ by remember { mutableFloatStateOf(0f) }
    var accelMag by remember { mutableFloatStateOf(0f) }

    var gyroX by remember { mutableFloatStateOf(0f) }
    var gyroY by remember { mutableFloatStateOf(0f) }
    var gyroZ by remember { mutableFloatStateOf(0f) }
    var gyroMag by remember { mutableFloatStateOf(0f) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    accelX = event.values[0]
                    accelY = event.values[1]
                    accelZ = event.values[2]
                    accelMag = sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
                } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    gyroX = event.values[0]
                    gyroY = event.values[1]
                    gyroZ = event.values[2]
                    gyroMag = sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sm.registerListener(listener, accel, 20_000)
        sm.registerListener(listener, gyro, 20_000)

        onDispose { sm.unregisterListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Live Sensor Lab (50 Hz)", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("ICM42632M IMU — Samsung Galaxy M35 5G", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Accelerometer (m/s²)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("X: ${String.format("%.3f", accelX)}")
                Text("Y: ${String.format("%.3f", accelY)}")
                Text("Z: ${String.format("%.3f", accelZ)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Magnitude ||a||: ${String.format("%.3f", accelMag)}", fontWeight = FontWeight.Bold)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gyroscope (rad/s)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("X: ${String.format("%.3f", gyroX)}")
                Text("Y: ${String.format("%.3f", gyroY)}")
                Text("Z: ${String.format("%.3f", gyroZ)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Angular Velocity ||ω||: ${String.format("%.3f", gyroMag)}", fontWeight = FontWeight.Bold)
            }
        }
    }
}
