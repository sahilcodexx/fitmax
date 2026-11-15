package com.fixmax.core

import com.fixmax.core.common.*
import com.fixmax.core.detection.*
import com.fixmax.core.processing.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class DspAndDetectionTest {

    @Test
    fun testMagnitudeCalculation() {
        // M = sqrt(1.2^2 + 0.8^2 + 9.5^2) ≈ 9.6088
        val sample = AccelerometerSample(timestampNs = 0L, x = 1.2f, y = 0.8f, z = 9.5f)
        val expected = sqrt(1.2f * 1.2f + 0.8f * 0.8f + 9.5f * 9.5f)
        assertEquals(expected, sample.magnitude, 0.001f)
        assertEquals(expected, MagnitudeCalculator.calculate(1.2f, 0.8f, 9.5f), 0.001f)
    }

    @Test
    fun testGravityFilterRemovesStaticComponent() {
        val filter = GravityFilter(cutoffHz = 0.30f, samplingRateHz = 50.0f)
        // Feed constant gravity 9.81 for 100 samples (2 seconds)
        var dynamic = 0f
        for (i in 0 until 100) {
            dynamic = filter.filter(9.81f, i * 20_000_000L)
        }
        // Static component should decay to near zero dynamic acceleration
        assertTrue("Static dynamic signal should be near 0, but was $dynamic", abs(dynamic) < 0.05f)
    }

    @Test
    fun testSignalFilterBandpass() {
        val filter = SignalFilter()
        // DC offset of 5.0f should be attenuated by High-Pass stage
        var out = 0f
        for (i in 0 until 60) {
            out = filter.filter(5.0f)
        }
        assertTrue("DC offset should be filtered out by HPF stage, was $out", abs(out) < 0.2f)
    }

    @Test
    fun testPeakDetectorIdentifiesPeaksWithProminence() {
        val detector = PeakDetector(minRefractorySamples = 2)
        // Synthetic signal with peaks
        val signal = floatArrayOf(0.1f, 0.2f, 0.8f, 0.2f, 0.1f, 0.15f, 0.9f, 0.15f, 0.1f)
        val timestamps = LongArray(signal.size) { it * 20_000_000L }

        val peaks = detector.findPeaks(signal, timestamps, isHandheld = false)
        assertEquals(2, peaks.size)
        assertEquals(2, peaks[0].indexInWindow)
        assertEquals(6, peaks[1].indexInWindow)
    }

    @Test
    fun testAutocorrelationDetectsWalkingCadence() {
        // Create 3 seconds (150 samples) of periodic walking signal at ~100 steps/min (1.67 Hz -> period ~30 samples)
        val n = 150
        val signal = FloatArray(n)
        for (i in 0 until n) {
            signal[i] = sin(2.0 * Math.PI * (i.toDouble() / 30.0)).toFloat()
        }

        val autocorr = Autocorrelation()
        val result = autocorr.analyze(signal)
        assertTrue("Expected high periodicity for pure sine wave, got ${result.peakPeriodicity}", result.peakPeriodicity > 0.80f)
        assertTrue("Expected cadence near 100 spm, got ${result.acfCadence}", abs(result.acfCadence - 100f) < 10f)
    }

    @Test
    fun testCadenceAnalyzer() {
        // Peaks at 0.0s, 0.6s, 1.2s, 1.8s, 2.4s (period 0.6s -> cadence 100 spm)
        val peaks = listOf(
            PeakCandidate(0, 0L, 1.0f, 0.5f, 0f, 0f),
            PeakCandidate(30, 600_000_000L, 1.0f, 0.5f, 0f, 0f),
            PeakCandidate(60, 1_200_000_000L, 1.0f, 0.5f, 0f, 0f),
            PeakCandidate(90, 1_800_000_000L, 1.0f, 0.5f, 0f, 0f),
            PeakCandidate(120, 2_400_000_000L, 1.0f, 0.5f, 0f, 0f)
        )

        val analyzer = CadenceAnalyzer()
        val metrics = analyzer.analyze(peaks)
        assertEquals(100f, metrics.cadenceStepsPerMinute, 2f)
        assertTrue("CV should be near 0 for uniform peaks", metrics.coefficientOfVariation < 0.05f)
    }

    @Test
    fun testFalseStepRejectionVehicleVibration() {
        val rawDyn = FloatArray(150) { if (it % 2 == 0) 1.5f else -1.5f } // High frequency vibration
        val cad = CadenceMetrics(0, 0f, 0f, 0f, 1f)
        val acf = AutocorrelationResult(0, 0.1f, 0f)

        val classifier = WalkingClassifier()
        val confidence = classifier.classify(rawDyn, cad, acf, gyroMag = 0f)

        assertEquals(ActivityState.VEHICLE_LIKE_MOTION, confidence.activityState)
        assertEquals(0f, confidence.compositeScore, 0.01f)
    }
}
