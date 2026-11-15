package com.fixmax.core.sensors

import android.content.Context
import com.fixmax.core.common.AccelerometerSample
import com.fixmax.core.common.GyroscopeSample
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Records timestamped sensor data to internal CSV storage for desktop verification & algorithm replay.
 */
class CsvSessionRecorder(private val context: Context) {

    private var writer: FileWriter? = null
    private var currentFile: File? = null
    var isRecording = false
        private set

    fun startSession(sessionLabel: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "fixmax_session_${sessionLabel}_$timestamp.csv"
        val dir = File(context.filesDir, "recorded_sessions").apply { mkdirs() }
        val file = File(dir, fileName)
        currentFile = file

        writer = FileWriter(file).apply {
            write("timestamp_ns,sensor_type,x,y,z,magnitude\n")
            flush()
        }
        isRecording = true
        return file
    }

    fun recordAccelerometer(sample: AccelerometerSample) {
        if (!isRecording) return
        writer?.write("${sample.timestampNs},ACCEL,${sample.x},${sample.y},${sample.z},${sample.magnitude}\n")
    }

    fun recordGyroscope(sample: GyroscopeSample) {
        if (!isRecording) return
        writer?.write("${sample.timestampNs},GYRO,${sample.x},${sample.y},${sample.z},${sample.angularVelocityMagnitude}\n")
    }

    fun stopSession(): File? {
        if (!isRecording) return null
        writer?.flush()
        writer?.close()
        writer = null
        isRecording = false
        return currentFile
    }
}
