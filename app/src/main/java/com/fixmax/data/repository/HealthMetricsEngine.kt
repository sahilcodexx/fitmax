package com.fixmax.data.repository

import com.fixmax.tracking.StepTrackingService
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

data class DailyHealthSnapshot(
    val dayLabel: String,         // "Mon", "Tue", "Wed"
    val dateNumber: String,       // "10", "11", "12"
    val fullDateStr: String,      // "yyyy-MM-dd"
    val steps: Long,              // Live real steps
    val goalSteps: Long = 10000L,
    val distanceKm: Float,        // Real computed distance in km
    val caloriesBurned: Int,      // Real computed active kcal
    val activeMinutes: Int,       // Real computed active minutes
    val goalActiveMinutes: Int = 60,
    val goalCalories: Int = 500,
    val heartRateBpm: Int = 68,
    val oxygenPercent: Float = 98.0f,
    val floors: Int = 0,
    val healthScore: Int = 0,
    val sleepScore: String = "--",
    val cardioLoad: String = "No Activity"
) {
    val stepsProgress: Float get() = (steps.toFloat() / goalSteps.coerceAtLeast(1L)).coerceIn(0f, 1.5f)
    val activeTimeProgress: Float get() = (activeMinutes.toFloat() / goalActiveMinutes.coerceAtLeast(1)).coerceIn(0f, 1.5f)
    val caloriesProgress: Float get() = (caloriesBurned.toFloat() / goalCalories.coerceAtLeast(1)).coerceIn(0f, 1.5f)

    val formattedSteps: String get() = java.text.NumberFormat.getNumberInstance(Locale.US).format(steps)
    val formattedDistance: String get() = String.format(Locale.US, "%.2f km", distanceKm)
    val formattedActiveTime: String get() = if (activeMinutes >= 60) "${activeMinutes / 60}h ${activeMinutes % 60}m" else "${activeMinutes}m"
}

object HealthMetricsEngine {
    // Kinematic & Metabolic Constants (stride length 0.832m, calorie rate 0.054 kcal/step)
    const val STRIDE_LENGTH_METERS = 0.832f   // Adult stride length
    const val KCAL_PER_STEP = 0.054f         // Caloric burn rate
    const val STEPS_PER_ACTIVE_MINUTE = 110f // ~110 steps/min cadence threshold for brisk active time
    const val STEPS_PER_FLOOR = 350L         // ~350 steps ≈ 1 floor elevation equivalent

    /**
     * Compute full health metrics given step count and user parameters.
     */
    fun computeSnapshot(
        steps: Long,
        dayLabel: String = "Wed",
        dateNumber: String = "18",
        fullDateStr: String = LocalDate.now().toString(),
        goalSteps: Long = 10000L,
        strideLength: Float = STRIDE_LENGTH_METERS,
        weightKg: Float = 68f
    ): DailyHealthSnapshot {
        val safeSteps = steps.coerceAtLeast(0L)
        val distanceKm = (safeSteps * strideLength) / 1000f
        val calories = (safeSteps * KCAL_PER_STEP * (weightKg / 70f)).roundToInt().coerceAtLeast(0)
        val activeMinutes = (safeSteps / STEPS_PER_ACTIVE_MINUTE).roundToInt().coerceAtLeast(0)
        val floors = if (safeSteps > 0) (safeSteps / STEPS_PER_FLOOR).toInt().coerceAtLeast(1) else 0

        // Dynamic physiological calculations
        val baseBpm = 68
        val dynamicBpm = if (safeSteps > 0) (baseBpm + (activeMinutes * 0.48f).coerceAtMost(52f)).roundToInt() else 68
        val oxygen = if (safeSteps > 0) (98.2f + (0.4f * kotlin.math.sin(safeSteps.toDouble() / 1200.0).toFloat())).coerceIn(95f, 100f) else 98.0f

        // Composite wellness score (0-100%)
        val stepPts = (safeSteps.toFloat() / goalSteps).coerceIn(0f, 1f) * 45f
        val timePts = (activeMinutes.toFloat() / 60f).coerceIn(0f, 1f) * 35f
        val calPts = (calories.toFloat() / 500f).coerceIn(0f, 1f) * 20f
        val compositeScore = if (safeSteps > 0) (stepPts + timePts + calPts).roundToInt().coerceIn(10, 100) else 0

        val cardioStatus = when {
            safeSteps == 0L -> "No Activity"
            activeMinutes >= 50 -> "Optimal"
            activeMinutes >= 25 -> "Maintaining"
            else -> "Low Load"
        }

        val sleep = when {
            safeSteps == 0L -> "--"
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

    const val PAST_DAYS_COUNT = 30
    const val FUTURE_DAYS_COUNT = 14
    const val TODAY_INDEX = 30

    /**
     * Generate dynamic full date history centered on referenceDate (Today at index 30).
     * Includes past 30 days and next 14 days (45 days total) so user can scroll across all dates.
     * Past days use historyMap (from DB), defaulting to 0 for unrecorded days.
     */
    fun generateWeeklyHistory(
        liveTodaySteps: Long,
        referenceDate: LocalDate = LocalDate.now(),
        historyMap: Map<String, Long> = emptyMap()
    ): List<DailyHealthSnapshot> {
        val todaySteps = liveTodaySteps.coerceAtLeast(0L)

        val offsets = (-PAST_DAYS_COUNT..FUTURE_DAYS_COUNT).toList()

        return offsets.map { offset ->
            val date = referenceDate.plusDays(offset.toLong())
            val dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
            val dateNum = date.dayOfMonth.toString()
            val fullDateStr = date.toString()

            val stepsForDay = when {
                offset == 0 -> todaySteps
                offset < 0 -> historyMap[fullDateStr] ?: 0L
                else -> 0L // Future days have 0 steps
            }

            computeSnapshot(
                steps = stepsForDay,
                dayLabel = dayLabel,
                dateNumber = dateNum,
                fullDateStr = fullDateStr
            )
        }
    }

    /**
     * Compute weekly points and progress percentage for the 7-day rolling window.
     */
    fun computeWeeklyPoints(history: List<DailyHealthSnapshot>, selectedIndex: Int = -1): Pair<Int, Int> {
        val targetIndex = if (selectedIndex in history.indices) selectedIndex else TODAY_INDEX.coerceIn(history.indices)
        val startIndex = (targetIndex - 6).coerceAtLeast(0)
        val slice = history.subList(startIndex, (targetIndex + 1).coerceAtMost(history.size))
        val totalPoints = slice.filter { it.steps > 0 }.sumOf { snapshot ->
            (snapshot.steps / 10L) + (snapshot.activeMinutes * 5L) + (snapshot.caloriesBurned / 2L)
        }.toInt()
        val weeklyTarget = 2400
        val percentage = ((totalPoints.toFloat() / weeklyTarget) * 100f).roundToInt().coerceIn(0, 100)
        return totalPoints to percentage
    }
}
