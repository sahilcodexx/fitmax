package com.fixmax.data.repository

import android.content.Context
import com.fixmax.data.database.DailySummaryEntity
import com.fixmax.data.database.FixMaxDatabase
import com.fixmax.data.database.SensorCheckpointEntity
import kotlinx.coroutines.flow.Flow

class StepRepository(private val db: FixMaxDatabase) {
    suspend fun getCheckpoint(): SensorCheckpointEntity? = db.checkpointDao().getCheckpoint()
    suspend fun saveCheckpoint(checkpoint: SensorCheckpointEntity) = db.checkpointDao().saveCheckpoint(checkpoint)

    fun observeToday(dateStr: String): Flow<DailySummaryEntity?> = db.dailySummaryDao().observeDailySummary(dateStr)
    suspend fun getDailySummary(dateStr: String): DailySummaryEntity? = db.dailySummaryDao().getDailySummary(dateStr)

    fun observeAllSummaries(): Flow<List<DailySummaryEntity>> = db.dailySummaryDao().observeAllSummaries()
    suspend fun getAllSummaries(): List<DailySummaryEntity> = db.dailySummaryDao().getAllSummaries()

    suspend fun addSteps(dateStr: String, delta: Long, stepLengthMeters: Float = 0.832f) {
        db.dailySummaryDao().incrementDailySteps(dateStr, delta, stepLengthMeters, System.currentTimeMillis())
    }

    suspend fun setTodaySteps(dateStr: String, steps: Long, stepLengthMeters: Float = 0.832f) {
        db.dailySummaryDao().setDailySteps(dateStr, steps, stepLengthMeters, System.currentTimeMillis())
    }

    companion object {
        @Volatile private var INSTANCE: StepRepository? = null
        fun getInstance(context: Context): StepRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StepRepository(FixMaxDatabase.getInstance(context)).also { INSTANCE = it }
            }
    }
}
