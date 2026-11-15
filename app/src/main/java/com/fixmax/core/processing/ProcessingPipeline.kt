package com.fixmax.core.processing

import com.fixmax.core.common.AccelerometerSample
import kotlin.math.exp
import kotlin.math.sqrt

object MagnitudeCalculator {
    fun calculate(sample: AccelerometerSample): Float = sample.magnitude
    fun calculate(x: Float, y: Float, z: Float): Float = sqrt(x * x + y * y + z * z)
}

class GravityFilter(cutoffHz: Float = 0.30f, samplingRateHz: Float = 50.0f) {
    private val dt = 1.0f / samplingRateHz
    private val alpha = exp(-2.0 * Math.PI * cutoffHz * dt).toFloat()
    private var gravity = 9.80665f
    private var lastTimestampNs = 0L
    private var initialized = false

    fun filter(magnitude: Float, timestampNs: Long): Float {
        if (!initialized) {
            gravity = magnitude
            lastTimestampNs = timestampNs
            initialized = true
            return 0.0f
        }
        val deltaNs = timestampNs - lastTimestampNs
        lastTimestampNs = timestampNs
        if (deltaNs > 50_000_000L || deltaNs < 0L) {
            gravity = magnitude
            return 0.0f
        }
        gravity = alpha * gravity + (1.0f - alpha) * magnitude
        return magnitude - gravity
    }
}

class SignalFilter(private val alphaHP: Float = 0.941f) {
    private val b0 = 0.0366f; private val b1 = 0.0731f; private val b2 = 0.0366f
    private val a1 = -1.3909f; private val a2 = 0.5372f
    private var prevD = 0f; private var prevYHp = 0f; private var hpInit = false
    private var x1 = 0f; private var x2 = 0f; private var y1 = 0f; private var y2 = 0f

    fun filter(d: Float): Float {
        val yHp = if (!hpInit) {
            prevD = d; prevYHp = 0f; hpInit = true; 0f
        } else {
            val res = alphaHP * (prevYHp + d - prevD)
            prevD = d; prevYHp = res; res
        }
        val y = b0 * yHp + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1; x1 = yHp; y2 = y1; y1 = y
        return y
    }
}

data class ProcessedWindow(
    val windowIndex: Long,
    val startTimestampNs: Long,
    val endTimestampNs: Long,
    val filteredSignal: FloatArray,
    val rawDynamicSignal: FloatArray,
    val timestampsNs: LongArray,
    val sampleCount: Int
)

class WindowManager(val windowSize: Int = 150, val hopSize: Int = 50) {
    private val filteredBuf = FloatArray(windowSize)
    private val dynamicBuf = FloatArray(windowSize)
    private val timeBuf = LongArray(windowSize)
    private var count = 0
    private var index = 0L
    private var hopCount = 0

    fun addSample(filtered: Float, dynamic: Float, timestampNs: Long): ProcessedWindow? {
        if (count < windowSize) {
            filteredBuf[count] = filtered
            dynamicBuf[count] = dynamic
            timeBuf[count] = timestampNs
            count++
            if (count == windowSize) {
                hopCount = 0
                index++
                return ProcessedWindow(index, timeBuf[0], timeBuf[windowSize - 1], filteredBuf.clone(), dynamicBuf.clone(), timeBuf.clone(), windowSize)
            }
            return null
        }
        System.arraycopy(filteredBuf, 1, filteredBuf, 0, windowSize - 1)
        System.arraycopy(dynamicBuf, 1, dynamicBuf, 0, windowSize - 1)
        System.arraycopy(timeBuf, 1, timeBuf, 0, windowSize - 1)
        filteredBuf[windowSize - 1] = filtered
        dynamicBuf[windowSize - 1] = dynamic
        timeBuf[windowSize - 1] = timestampNs
        hopCount++
        if (hopCount >= hopSize) {
            hopCount = 0
            index++
            return ProcessedWindow(index, timeBuf[0], timeBuf[windowSize - 1], filteredBuf.clone(), dynamicBuf.clone(), timeBuf.clone(), windowSize)
        }
        return null
    }
}
