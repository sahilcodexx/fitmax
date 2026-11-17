package com.fixmax.data.repository

import com.fixmax.data.database.DailySummaryEntity
import com.fixmax.data.database.FixMaxDatabase
import com.fixmax.data.database.SensorCheckpointEntity
import kotlinx.coroutines.flow.Flow

class StepRepository(private val db: FixMaxDatabase) {
    suspend fun getCheckpoint(): SensorCheckpointEntity? = db.checkpointDao().getCheckpoint()
    suspend fun saveCheckpoint(checkpoint: SensorCheckpointEntity) = db.checkpointDao().saveCheckpoint(checkpoint)

    fun observeToday(dateStr: String): Flow<DailySummaryEntity?> = db.dailySummaryDao().observeDailySummary(dateStr)

    suspend fun addSteps(dateStr: String, delta: Long, stepLengthMeters: Float = 0.832f) {
        db.dailySummaryDao().incrementDailySteps(dateStr, delta, stepLengthMeters, System.currentTimeMillis())
    }

    suspend fun setTodaySteps(dateStr: String, steps: Long, stepLengthMeters: Float = 0.832f) {
        db.dailySummaryDao().setDailySteps(dateStr, steps, stepLengthMeters, System.currentTimeMillis())
    }
}
