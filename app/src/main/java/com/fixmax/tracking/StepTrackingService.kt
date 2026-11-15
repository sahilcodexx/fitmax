package com.fixmax.tracking

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.fixmax.core.common.TrackingMode
import com.fixmax.core.sensors.SensorDiscovery
import com.fixmax.data.database.FixMaxDatabase
import com.fixmax.data.database.SensorCheckpointEntity
import com.fixmax.data.repository.StepRepository
import kotlinx.coroutines.*
import java.time.LocalDate

class StepTrackingService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID = "fixmax_tracking_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.fixmax.START"
        const val ACTION_STOP = "com.fixmax.STOP"

        fun start(context: Context) {
            val intent = Intent(context, StepTrackingService::class.java).apply { action = ACTION_START }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, StepTrackingService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }
    }

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var sensorManager: SensorManager
    private lateinit var handlerThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private var wakeLock: PowerManager.WakeLock? = null

    private lateinit var repository: StepRepository
    private var lastHardwareSteps = -1L
    private var baseOffset = 0L

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        handlerThread = HandlerThread("FixMaxSensorThread", Process.THREAD_PRIORITY_MORE_FAVORABLE).apply { start() }
        backgroundHandler = Handler(handlerThread.looper)
        repository = StepRepository(FixMaxDatabase.getInstance(this))

        createChannel()
        scope.launch {
            val cp = repository.getCheckpoint()
            if (cp != null) {
                lastHardwareSteps = cp.lastCumulativeHardwareReading
                baseOffset = cp.accumulatedStepsBeforeReboot
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val notif = buildNotification(baseOffset)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notif)
        }

        registerSensors()
        return START_STICKY
    }

    private fun registerSensors() {
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounter != null) {
            // Samsung hardware step counter active
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL, 10_000_000, backgroundHandler)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val current = event.values[0].toLong()
            if (lastHardwareSteps == -1L) {
                lastHardwareSteps = current
                return
            }
            if (current < lastHardwareSteps) {
                // Phone rebooted
                baseOffset += lastHardwareSteps
                lastHardwareSteps = current
            }
            val delta = current - lastHardwareSteps
            if (delta > 0) {
                lastHardwareSteps = current
                baseOffset += delta
                scope.launch {
                    val today = LocalDate.now().toString()
                    repository.addSteps(today, delta)
                    repository.saveCheckpoint(SensorCheckpointEntity(1, System.currentTimeMillis(), current, baseOffset, SystemClock.elapsedRealtime()))
                    updateNotification(baseOffset)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "FIX MAX Tracking", NotificationManager.IMPORTANCE_LOW)
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(steps: Long): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FIX MAX Step Tracking")
            .setContentText("Today: $steps steps")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(steps: Long) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(steps))
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        wakeLock?.let { if (it.isHeld) it.release() }
        handlerThread.quitSafely()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
