package com.fixmax.feature.recorder

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
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
    var sampleCount by remember { mutableLongStateOf(0L) }

    DisposableEffect(isRecording) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val thread = HandlerThread("RecorderThread").apply { start() }
        val handler = Handler(thread.looper)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (!isRecording) return
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    recorder.recordAccelerometer(
                        AccelerometerSample(event.timestamp, event.values[0], event.values[1], event.values[2])
                    )
                    sampleCount++
                } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    recorder.recordGyroscope(
                        GyroscopeSample(event.timestamp, event.values[0], event.values[1], event.values[2])
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (isRecording) {
            sm.registerListener(listener, accel, 20_000, handler)
            sm.registerListener(listener, gyro, 20_000, handler)
        }

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
        Text("Sensor Recorder & CSV Exporter", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Record real walking sessions to CSV for offline algorithm training and replay.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = sessionLabel,
            onValueChange = { sessionLabel = it },
            label = { Text("Session Label") },
            placeholder = { Text("e.g. handheld_texting, pocket, running") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (!isRecording) {
                    sampleCount = 0L
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isRecording) "Stop & Finalize Recording" else "Start Recording Session")
        }

        if (isRecording) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recording active...", fontWeight = FontWeight.Bold)
                    Text("$sampleCount samples captured", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        currentFile?.let { file ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Session File Created:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(file.name, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("Path: ${file.absolutePath}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Size: ${file.length() / 1024} KB", fontSize = 12.sp)

                    Button(
                        onClick = {
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share CSV Recording"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export / Share CSV File")
                    }
                }
            }
        }
    }
}
