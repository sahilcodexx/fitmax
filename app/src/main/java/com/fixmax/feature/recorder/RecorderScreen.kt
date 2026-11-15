package com.fixmax.feature.recorder

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
import com.fixmax.core.common.AccelerometerSample
import com.fixmax.core.common.GyroscopeSample
import com.fixmax.core.sensors.CsvSessionRecorder
import java.io.File

@Composable
fun RecorderScreen() {
    val context = LocalContext.current
    val recorder = remember { CsvSessionRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var sessionLabel by remember { mutableStateOf("walking_pocket") }

    DisposableEffect(isRecording) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!isRecording) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    recorder.recordAccelerometer(
                        AccelerometerSample(event.timestamp, event.values[0], event.values[1], event.values[2])
                    )
                } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    recorder.recordGyroscope(
                        GyroscopeSample(event.timestamp, event.values[0], event.values[1], event.values[2])
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (isRecording) {
            sm.registerListener(listener, accel, 20_000)
            sm.registerListener(listener, gyro, 20_000)
        }

        onDispose { sm.unregisterListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Sensor Recorder & CSV Exporter", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Record real walking sessions to CSV for offline algorithm training.", fontSize = 13.sp)

        OutlinedTextField(
            value = sessionLabel,
            onValueChange = { sessionLabel = it },
            label = { Text("Session Label (e.g. handheld_texting, pocket, running)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (!isRecording) {
                    currentFile = recorder.startSession(sessionLabel)
                    isRecording = true
                } else {
                    currentFile = recorder.stopSession()
                    isRecording = false
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRecording) "Stop & Save Recording" else "Start Recording Session")
        }

        currentFile?.let { file ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Session Saved:", fontWeight = FontWeight.Bold)
                    Text(file.absolutePath, fontSize = 12.sp)
                    Text("File size: ${file.length() / 1024} KB")
                }
            }
        }
    }
}
