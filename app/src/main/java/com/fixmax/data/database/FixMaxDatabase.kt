package com.fixmax.data.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "device_profiles")
data class DeviceProfileEntity(
    @PrimaryKey val id: Int = 1,
    val deviceModel: String,
    val androidVersion: Int,
    val accelName: String,
    val gyroName: String,
    val stepCounterName: String,
    val hasHardwareStepCounter: Boolean,
    val hasHardwareStepDetector: Boolean,
    val recommendedTrackingMode: String
)

@Entity(tableName = "sensor_checkpoints")
data class SensorCheckpointEntity(
    @PrimaryKey val id: Int = 1,
    val timestampMs: Long,
    val lastCumulativeHardwareReading: Long,
    val accumulatedStepsBeforeReboot: Long,
    val bootTimestampMs: Long
)

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val date: String,
    val totalSteps: Long,
    val distanceMeters: Float,
    val activeMinutes: Int,
    val goalSteps: Long = 10_000L,
    val lastUpdatedMs: Long
)

@Dao
interface SensorCheckpointDao {
    @Query("SELECT * FROM sensor_checkpoints WHERE id = 1 LIMIT 1")
    suspend fun getCheckpoint(): SensorCheckpointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCheckpoint(checkpoint: SensorCheckpointEntity)
}

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    fun observeDailySummary(date: String): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summaries WHERE date = :date LIMIT 1")
    suspend fun getDailySummary(date: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries")
    fun observeAllSummaries(): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summaries")
    suspend fun getAllSummaries(): List<DailySummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(summary: DailySummaryEntity)

    @Transaction
    suspend fun incrementDailySteps(date: String, delta: Long, stepLengthMeters: Float, timestampMs: Long) {
        val current = getDailySummary(date)
        if (current == null) {
            insertOrUpdate(DailySummaryEntity(date, delta, delta * stepLengthMeters, 0, 10_000L, timestampMs))
        } else {
            val total = current.totalSteps + delta
            insertOrUpdate(current.copy(totalSteps = total, distanceMeters = total * stepLengthMeters, lastUpdatedMs = timestampMs))
        }
    }

    @Transaction
    suspend fun setDailySteps(date: String, steps: Long, stepLengthMeters: Float = 0.832f, timestampMs: Long) {
        val current = getDailySummary(date)
        val distance = steps * stepLengthMeters
        val activeMins = (steps / 110f).toInt()
        if (current == null) {
            insertOrUpdate(DailySummaryEntity(date, steps, distance, activeMins, 10_000L, timestampMs))
        } else {
            insertOrUpdate(current.copy(totalSteps = steps, distanceMeters = distance, activeMinutes = activeMins, lastUpdatedMs = timestampMs))
        }
    }
}

@Database(entities = [DeviceProfileEntity::class, SensorCheckpointEntity::class, DailySummaryEntity::class], version = 1, exportSchema = false)
abstract class FixMaxDatabase : RoomDatabase() {
    abstract fun checkpointDao(): SensorCheckpointDao
    abstract fun dailySummaryDao(): DailySummaryDao

    companion object {
        @Volatile private var INSTANCE: FixMaxDatabase? = null
        fun getInstance(context: Context): FixMaxDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, FixMaxDatabase::class.java, "fixmax.db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
