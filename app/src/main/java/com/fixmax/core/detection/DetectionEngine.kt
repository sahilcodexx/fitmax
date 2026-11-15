package com.fixmax.core.detection

import com.fixmax.core.common.*
import kotlin.math.*

class PeakDetector(private val minRefractorySamples: Int = 12) {
    fun findPeaks(signal: FloatArray, timestampsNs: LongArray, isHandheld: Boolean): List<PeakCandidate> {
        val minHeight = if (isHandheld) 0.12f else 0.25f
        val minProm = if (isHandheld) 0.10f else 0.20f
        val candidates = mutableListOf<PeakCandidate>()
        val n = signal.size
        var lastPeak = -minRefractorySamples

        for (i in 1 until n - 1) {
            val curr = signal[i]
            if (curr > signal[i - 1] && curr >= signal[i + 1] && curr >= minHeight) {
                if (i - lastPeak < minRefractorySamples) continue

                var vL = curr
                for (j in i - 1 downTo 0) {
                    vL = min(vL, signal[j])
                    if (signal[j] > curr) break
                }
                var vR = curr
                for (j in i + 1 until n) {
                    vR = min(vR, signal[j])
                    if (signal[j] > curr) break
                }
                val prom = curr - max(vL, vR)
                if (prom >= minProm) {
                    candidates.add(PeakCandidate(i, timestampsNs[i], curr, prom, vL, vR))
                    lastPeak = i
                }
            }
        }
        return candidates
    }
}

class Autocorrelation(private val minLag: Int = 14, private val maxLag: Int = 100, private val fs: Float = 50.0f) {
    fun analyze(signal: FloatArray): AutocorrelationResult {
        val n = signal.size
        if (n <= maxLag) return AutocorrelationResult(0, 0f, 0f)
        var sum = 0f
        for (v in signal) sum += v
        val mean = sum / n
        val zm = FloatArray(n) { signal[it] - mean }

        var maxR = -1f; var bestLag = 0
        for (lag in minLag..maxLag) {
            var sProd = 0.0; var sX = 0.0; var sY = 0.0
            val limit = n - lag
            for (i in 0 until limit) {
                val x = zm[i].toDouble(); val y = zm[i + lag].toDouble()
                sProd += x * y; sX += x * x; sY += y * y
            }
            val denom = sqrt(sX * sY)
            val r = if (denom > 1e-9) (sProd / denom).toFloat() else 0f
            if (r > maxR) { maxR = r; bestLag = lag }
        }
        val cad = if (bestLag > 0) (60f * fs) / bestLag else 0f
        return AutocorrelationResult(bestLag, maxR.coerceIn(-1f, 1f), cad)
    }
}

class CadenceAnalyzer {
    fun analyze(peaks: List<PeakCandidate>): CadenceMetrics {
        if (peaks.size < 2) return CadenceMetrics(peaks.size, 0f, 0f, 0f, 1f)
        val intervals = FloatArray(peaks.size - 1)
        var sum = 0f
        for (i in 1 until peaks.size) {
            val dt = (peaks[i].timestampNs - peaks[i - 1].timestampNs) / 1e9f
            intervals[i - 1] = dt; sum += dt
        }
        val avg = sum / intervals.size
        val cad = if (avg > 0f) 60f / avg else 0f
        var sumSq = 0.0
        for (dt in intervals) sumSq += (dt - avg) * (dt - avg)
        val std = sqrt(sumSq / intervals.size).toFloat()
        return CadenceMetrics(peaks.size, avg, cad, std, if (avg > 0f) std / avg else 1f)
    }
}

class WalkingClassifier(private val fs: Float = 50.0f) {
    fun classify(rawDyn: FloatArray, cad: CadenceMetrics, acf: AutocorrelationResult, gyroMag: Float): WalkingConfidence {
        val n = rawDyn.size
        var sum = 0.0; var sumSq = 0.0
        for (v in rawDyn) { sum += v; sumSq += v * v }
        val variance = (sumSq / n) - ((sum / n) * (sum / n))
        val sigmaD = sqrt(max(0.0, variance)).toFloat()

        var zc = 0
        for (i in 1 until n) if ((rawDyn[i] >= 0f && rawDyn[i - 1] < 0f) || (rawDyn[i] < 0f && rawDyn[i - 1] >= 0f)) zc++
        val zcrHz = (fs * zc) / (2f * (n - 1))

        if (zcrHz > 7.0f && acf.peakPeriodicity < 0.35f) {
            return WalkingConfidence(0f, ActivityState.VEHICLE_LIKE_MOTION, false, 0f, 0f, 0f, 0f, 0f, sigmaD, zcrHz)
        }
        if (sigmaD < 0.08f) {
            return WalkingConfidence(0f, ActivityState.STATIONARY, false, 0f, 0f, 0f, 0f, 0f, sigmaD, zcrHz)
        }

        val isHandheld = sigmaD in 0.10f..0.45f && acf.peakPeriodicity >= 0.50f && acf.acfCadence in 80f..140f
        val sPeriod = ((acf.peakPeriodicity - 0.20f) / 0.50f).coerceIn(0f, 1f)
        val sCv = (1f - (cad.coefficientOfVariation / 0.25f)).coerceIn(0f, 1f)
        val c = if (cad.cadenceStepsPerMinute > 0f) cad.cadenceStepsPerMinute else acf.acfCadence
        val sRange = when {
            c in 50f..190f -> 1f
            c in 35f..50f -> (c - 35f) / 15f
            c in 190f..220f -> (220f - c) / 30f
            else -> 0f
        }.coerceIn(0f, 1f)
        val sEnergy = (((sigmaD - 0.08f) / 0.12f).coerceIn(0f, 1f) * (1f - ((sigmaD - 10f) / 6f).coerceIn(0f, 1f))).coerceIn(0f, 1f)
        val sAgree = if (cad.cadenceStepsPerMinute > 0f && acf.acfCadence > 0f) (1f - (abs(cad.cadenceStepsPerMinute - acf.acfCadence) / 35f)).coerceIn(0f, 1f) else 0.5f
        val shake = if (gyroMag > 3.5f && cad.coefficientOfVariation > 0.30f) 0.5f else 1.0f

        val composite = (0.30f * sPeriod + 0.25f * sCv + 0.15f * sRange + 0.15f * sEnergy + 0.15f * sAgree) * shake
        val finalComp = composite.coerceIn(0f, 1f)
        val act = when {
            finalComp >= 0.65f -> if (c > 155f && sigmaD > 3.0f) ActivityState.RUNNING else ActivityState.WALKING
            finalComp >= 0.40f -> ActivityState.UNKNOWN
            else -> ActivityState.STATIONARY
        }

        return WalkingConfidence(finalComp, act, isHandheld, sPeriod, sCv, sRange, sEnergy, sAgree, sigmaD, zcrHz)
    }
}

class StepValidator(private val minConfidence: Float = 0.60f, private val debounceSteps: Int = 4) {
    private var lastCommittedNs = 0L
    private val buffer = mutableListOf<PeakCandidate>()
    private var walkingWindows = 0

    fun validate(peaks: List<PeakCandidate>, conf: WalkingConfidence): List<ConfirmedStep> {
        val isWalking = (conf.activityState == ActivityState.WALKING || conf.activityState == ActivityState.RUNNING) && conf.compositeScore >= minConfidence
        if (!isWalking) {
            walkingWindows = 0; buffer.clear(); return emptyList()
        }
        walkingWindows++
        val newPeaks = peaks.filter { it.timestampNs > lastCommittedNs + 250_000_000L }
        if (walkingWindows < 2 && buffer.size + newPeaks.size < debounceSteps) {
            buffer.addAll(newPeaks); return emptyList()
        }
        val out = mutableListOf<ConfirmedStep>()
        if (buffer.isNotEmpty()) {
            for (p in buffer) if (p.timestampNs > lastCommittedNs + 250_000_000L) {
                out.add(ConfirmedStep(p.timestampNs, conf.compositeScore, 100f, "CUSTOM_DSP"))
                lastCommittedNs = p.timestampNs
            }
            buffer.clear()
        }
        for (p in newPeaks) if (p.timestampNs > lastCommittedNs + 250_000_000L) {
            out.add(ConfirmedStep(p.timestampNs, conf.compositeScore, 100f, "CUSTOM_DSP"))
            lastCommittedNs = p.timestampNs
        }
        return out
    }
}
