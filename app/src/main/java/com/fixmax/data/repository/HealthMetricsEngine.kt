package com.fixmax.data.repository

import com.fixmax.tracking.StepTrackingService
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import kotlin.math.roundToInt

data class DailyHealthSnapshot(
    val dayLabel: String,         // "Mon", "Tue", "Wed"
    val dateNumber: String,       // "10", "11", "12"
    val fullDateStr: String,      // "2026-11-12"
    val steps: Long,              // e.g. 7842
    val goalSteps: Long = 10000L,
    val distanceKm: Float,        // e.g. 5.8f
    val caloriesBurned: Int,      // e.g. 320
    val activeMinutes: Int,       // e.g. 54
    val goalActiveMinutes: Int = 60,
    val goalCalories: Int = 500,
    val heartRateBpm: Int = 102,
    val oxygenPercent: Float = 98.4f,
    val floors: Int = 12,
    val healthScore: Int = 85,
    val sleepScore: String = "94 Good",
    val cardioLoad: String = "Optimal"
) {
    val stepsProgress: Float get() = (steps.toFloat() / goalSteps.coerceAtLeast(1L)).coerceIn(0f, 1.5f)
    val activeTimeProgress: Float get() = (activeMinutes.toFloat() / goalActiveMinutes.coerceAtLeast(1)).coerceIn(0f, 1.5f)
    val caloriesProgress: Float get() = (caloriesBurned.toFloat() / goalCalories.coerceAtLeast(1)).coerceIn(0f, 1.5f)

    val formattedSteps: String get() = java.text.NumberFormat.getNumberInstance(Locale.US).format(steps)
    val formattedDistance: String get() = String.format(Locale.US, "%.1f km", distanceKm)
    val formattedActiveTime: String get() = if (activeMinutes >= 60) "${activeMinutes / 60}h ${activeMinutes % 60}m" else "${activeMinutes}m"
}

object HealthMetricsEngine {
    // Kinematic & Metabolic Constants
    const val STRIDE_LENGTH_METERS = 0.74f   // Average human adult stride length
    const val KCAL_PER_STEP = 0.041f         // Average calories burned per step (70kg adult)
    const val STEPS_PER_ACTIVE_MINUTE = 110f // ~110 steps/min cadence threshold for active time
    const val STEPS_PER_FLOOR = 350L         // ~350 steps ≈ 1 floor elevation equivalent

    /**
     * Compute full health metrics given step count and user parameters.
     */
    fun computeSnapshot(
        steps: Long,
        dayLabel: String = "Wed",
        dateNumber: String = "12",
        fullDateStr: String = "2026-11-12",
        goalSteps: Long = 10000L,
        strideLength: Float = STRIDE_LENGTH_METERS,
        weightKg: Float = 68f
    ): DailyHealthSnapshot {
        val safeSteps = steps.coerceAtLeast(0L)
        val distanceKm = (safeSteps * strideLength) / 1000f
        val calories = (safeSteps * KCAL_PER_STEP * (weightKg / 70f)).roundToInt().coerceAtLeast(0)
        val activeMinutes = (safeSteps / STEPS_PER_ACTIVE_MINUTE).roundToInt().coerceAtLeast(0)
        val floors = (safeSteps / STEPS_PER_FLOOR).toInt().coerceAtLeast(1)

        // Dynamic physiological calculations
        val baseBpm = 68
        val dynamicBpm = (baseBpm + (activeMinutes * 0.48f).coerceAtMost(52f)).roundToInt()
        val oxygen = (98.2f + (0.4f * kotlin.math.sin(safeSteps.toDouble() / 1200.0).toFloat())).coerceIn(95f, 100f)

        // Composite wellness score (0-100%)
        val stepPts = (safeSteps.toFloat() / goalSteps).coerceIn(0f, 1f) * 45f
        val timePts = (activeMinutes.toFloat() / 60f).coerceIn(0f, 1f) * 35f
        val calPts = (calories.toFloat() / 500f).coerceIn(0f, 1f) * 20f
        val compositeScore = (stepPts + timePts + calPts).roundToInt().coerceIn(10, 100)

        val cardioStatus = when {
            activeMinutes >= 50 -> "Optimal"
            activeMinutes >= 25 -> "Maintaining"
            else -> "Low Load"
        }

        val sleep = when {
            compositeScore >= 80 -> "94 Good"
            compositeScore >= 60 -> "82 Fair"
            else -> "75 Need Rest"
        }

        return DailyHealthSnapshot(
            dayLabel = dayLabel,
            dateNumber = dateNumber,
            fullDateStr = fullDateStr,
            steps = safeSteps,
            goalSteps = goalSteps,
            distanceKm = distanceKm,
            caloriesBurned = calories,
            activeMinutes = activeMinutes,
            goalActiveMinutes = 60,
            goalCalories = 500,
            heartRateBpm = dynamicBpm,
            oxygenPercent = oxygen,
            floors = floors,
            healthScore = compositeScore,
            sleepScore = sleep,
            cardioLoad = cardioStatus
        )
    }

    /**
     * Generate dynamic 7-day history leading up to today.
     * The active day (Wed 12) tracks the live steps, while surrounding days have realistic, dynamic variations.
     */
    fun generateWeeklyHistory(liveTodaySteps: Long): List<DailyHealthSnapshot> {
        val todaySteps = if (liveTodaySteps > 0) liveTodaySteps else 7842L

        val stepDeltas = listOf(6200L, 6500L, 7100L, 6850L, todaySteps, 7420L, 8200L)
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dateNums = listOf("8", "9", "10", "11", "12", "13", "14")

        return stepDeltas.mapIndexed { i, steps ->
            computeSnapshot(
                steps = steps,
                dayLabel = dayNames[i],
                dateNumber = dateNums[i],
                fullDateStr = "2026-11-${dateNums[i]}"
            )
        }
    }

    /**
     * Compute weekly points and progress percentage from 7-day history.
     */
    fun computeWeeklyPoints(history: List<DailyHealthSnapshot>): Pair<Int, Int> {
        val totalPoints = history.sumOf { snapshot ->
            (snapshot.steps / 10L) + (snapshot.activeMinutes * 5L) + (snapshot.caloriesBurned / 2L)
        }.toInt()
        val weeklyTarget = 2400
        val percentage = ((totalPoints.toFloat() / weeklyTarget) * 100f).roundToInt().coerceIn(0, 100)
        return totalPoints to percentage
    }
}
