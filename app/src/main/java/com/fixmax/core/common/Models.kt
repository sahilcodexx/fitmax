package com.fixmax.core.common

import kotlin.math.sqrt

data class AccelerometerSample(
    val timestampNs: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val accuracy: Int = 3
) {
    val magnitude: Float get() = sqrt(x * x + y * y + z * z)
}

data class GyroscopeSample(
    val timestampNs: Long,
    val x: Float,
    val y: Float,
    val z: Float,
    val accuracy: Int = 3
) {
    val angularVelocityMagnitude: Float get() = sqrt(x * x + y * y + z * z)
}

data class HardwareStepSample(
    val timestampNs: Long,
    val cumulativeSteps: Long,
    val isDetectorEvent: Boolean = false
)

enum class TrackingMode {
    HARDWARE_LOW_POWER,
    CUSTOM_DSP,
    DUAL_RECONCILIATION
}

enum class ActivityState {
    STATIONARY,
    WALKING,
    RUNNING,
    VEHICLE_LIKE_MOTION,
    UNKNOWN
}

data class PeakCandidate(
    val indexInWindow: Int,
    val timestampNs: Long,
    val peakValue: Float,
    val prominence: Float,
    val leftValley: Float,
    val rightValley: Float
)

data class AutocorrelationResult(
    val peakLag: Int,
    val peakPeriodicity: Float,
    val acfCadence: Float
)

data class CadenceMetrics(
    val peakCount: Int,
    val averageIntervalSec: Float,
    val cadenceStepsPerMinute: Float,
    val stdDevIntervalSec: Float,
    val coefficientOfVariation: Float
)

data class WalkingConfidence(
    val compositeScore: Float,
    val activityState: ActivityState,
    val isHandheldTextingMode: Boolean,
    val periodicityScore: Float,
    val cadenceCvScore: Float,
    val cadenceRangeScore: Float,
    val energyScore: Float,
    val cadenceAgreementScore: Float,
    val signalStdDev: Float,
    val zeroCrossingRateHz: Float
)

data class ConfirmedStep(
    val timestampNs: Long,
    val confidence: Float,
    val cadenceSpm: Float,
    val source: String
)
