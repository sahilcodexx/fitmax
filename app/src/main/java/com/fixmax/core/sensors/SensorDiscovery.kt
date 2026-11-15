package com.fixmax.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import com.fixmax.core.common.TrackingMode

data class DeviceCapabilityReport(
    val deviceModel: String,
    val androidVersion: Int,
    val accelName: String,
    val accelVendor: String,
    val gyroName: String,
    val gyroVendor: String,
    val stepCounterName: String,
    val hasHardwareStepCounter: Boolean,
    val hasHardwareStepDetector: Boolean,
    val hasLinearAcceleration: Boolean,
    val accelResolution: Float,
    val accelMaxRange: Float,
    val accelMinDelayUs: Int,
    val recommendedTrackingMode: TrackingMode
)

class SensorDiscovery(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun inspect(): DeviceCapabilityReport {
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        val hasStepCounter = stepCounter != null
        val recommendedMode = if (hasStepCounter) TrackingMode.HARDWARE_LOW_POWER else TrackingMode.CUSTOM_DSP

        return DeviceCapabilityReport(
            deviceModel = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (${android.os.Build.DEVICE})",
            androidVersion = android.os.Build.VERSION.SDK_INT,
            accelName = accel?.name ?: "None",
            accelVendor = accel?.vendor ?: "None",
            gyroName = gyro?.name ?: "None",
            gyroVendor = gyro?.vendor ?: "None",
            stepCounterName = stepCounter?.name ?: "None",
            hasHardwareStepCounter = hasStepCounter,
            hasHardwareStepDetector = stepDetector != null,
            hasLinearAcceleration = linearAccel != null,
            accelResolution = accel?.resolution ?: 0f,
            accelMaxRange = accel?.maximumRange ?: 0f,
            accelMinDelayUs = accel?.minDelay ?: 0,
            recommendedTrackingMode = recommendedMode
        )
    }
}
