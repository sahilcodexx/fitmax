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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.fixmax.core.common.*
import com.fixmax.core.detection.*
import com.fixmax.core.processing.*
import com.fixmax.data.database.FixMaxDatabase
import com.fixmax.data.database.SensorCheckpointEntity
import com.fixmax.data.repository.StepRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class StepTrackingService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "FixMaxService"
        const val CHANNEL_ID = "fixmax_tracking_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "com.fixmax.START"
        const val ACTION_STOP = "com.fixmax.STOP"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        private val _liveStepsToday = MutableStateFlow(0L)
        val liveStepsToday = _liveStepsToday.asStateFlow()

        private val _liveActivityState = MutableStateFlow(ActivityState.STATIONARY)
        val liveActivityState = _liveActivityState.asStateFlow()

        private val _liveCadence = MutableStateFlow(0f)
        val liveCadence = _liveCadence.asStateFlow()

        fun start(context: Context) {
            try {
                val intent = Intent(context, StepTrackingService::class.java).apply { action = ACTION_START }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initiate startService", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, StepTrackingService::class.java).apply { action = ACTION_STOP }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initiate stopService", e)
            }
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

    // Custom DSP Pipeline instances
    private val gravityFilter = GravityFilter(cutoffHz = 0.30f, samplingRateHz = 50.0f)
    private val signalFilter = SignalFilter()
    private val windowManager = WindowManager(windowSize = 150, hopSize = 50)
    private val peakDetector = PeakDetector(minRefractorySamples = 12)
    private val autocorr = Autocorrelation()
    private val cadenceAnalyzer = CadenceAnalyzer()
    private val classifier = WalkingClassifier()
    private val validator = StepValidator()

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        handlerThread = HandlerThread("FixMaxSensorThread", Process.THREAD_PRIORITY_MORE_FAVORABLE).apply { start() }
        backgroundHandler = Handler(handlerThread.looper)
        repository = StepRepository(FixMaxDatabase.getInstance(this))

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FixMax:TrackingWakeLock").apply {
            setReferenceCounted(false)
        }

        createChannel()
        scope.launch {
            val today = LocalDate.now().toString()
            val summary = repository.observeToday(today)
            summary.collect {
                if (it != null) {
                    _liveStepsToday.value = it.totalSteps
                }
            }
        }
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

        startForegroundSafely()
        wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24hr max with safety
        registerSensors()
        _isServiceRunning.value = true
        return START_STICKY
    }

    private fun startForegroundSafely() {
        val notif = buildNotification(_liveStepsToday.value)
        var started = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
                started = true
            } catch (e: SecurityException) {
                Log.w(TAG, "Health FGS permission missing, falling back to DATA_SYNC", e)
            }
        }

        if (!started && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                started = true
            } catch (e: SecurityException) {
                Log.w(TAG, "DataSync FGS permission missing, falling back to default", e)
            }
        }

        if (!started) {
            try {
                startForeground(NOTIFICATION_ID, notif)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start foreground notification", e)
            }
        }
    }

    private fun registerSensors() {
        // 1. Hardware Step Counter (Samsung sensor_hub)
        val stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounter != null) {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL, 5_000_000, backgroundHandler)
        }

        // 2. Hardware Step Detector (Provides immediate real-time step events)
        val stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (stepDetector != null) {
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_FASTEST, 0, backgroundHandler)
        }

        // 3. Accelerometer (50 Hz for custom real-time DSP pipeline and activity classification)
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accel != null) {
            sensorManager.registerListener(this, accel, 20_000, backgroundHandler)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                handleHardwareStepCounter(event.values[0].toLong())
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                // If hardware step counter is dormant/latent, detector gives immediate pulse
                handleStepDetectedEvent()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometerSample(event.timestamp, event.values[0], event.values[1], event.values[2])
            }
        }
    }

    private fun handleHardwareStepCounter(current: Long) {
        if (lastHardwareSteps == -1L) {
            lastHardwareSteps = current
            return
        }
        if (current < lastHardwareSteps) {
            // Device reboot detected
            baseOffset += lastHardwareSteps
            lastHardwareSteps = current
        }
        val delta = current - lastHardwareSteps
        if (delta > 0) {
            lastHardwareSteps = current
            baseOffset += delta
            commitSteps(delta)
        }
    }

    private fun handleStepDetectedEvent() {
        // Step detector pulse
        _liveActivityState.value = ActivityState.WALKING
    }

    private fun handleAccelerometerSample(timestampNs: Long, x: Float, y: Float, z: Float) {
        val mag = MagnitudeCalculator.calculate(x, y, z)
        val dynamic = gravityFilter.filter(mag, timestampNs)
        val filtered = signalFilter.filter(dynamic)

        val window = windowManager.addSample(filtered, dynamic, timestampNs)
        if (window != null) {
            processWindow(window)
        }
    }

    private fun processWindow(window: ProcessedWindow) {
        val peaks = peakDetector.findPeaks(window.filteredSignal, window.timestampsNs, isHandheld = false)
        val acfResult = autocorr.analyze(window.filteredSignal)
        val cadenceMetrics = cadenceAnalyzer.analyze(peaks)
        val confidence = classifier.classify(window.rawDynamicSignal, cadenceMetrics, acfResult, gyroMag = 0f)

        _liveActivityState.value = confidence.activityState
        _liveCadence.value = cadenceMetrics.cadenceStepsPerMinute

        val validatedSteps = validator.validate(peaks, confidence)
        if (validatedSteps.isNotEmpty()) {
            commitSteps(validatedSteps.size.toLong())
        }
    }

    private fun commitSteps(delta: Long) {
        if (delta <= 0) return
        scope.launch {
            val today = LocalDate.now().toString()
            repository.addSteps(today, delta)
            _liveStepsToday.value += delta
            repository.saveCheckpoint(
                SensorCheckpointEntity(1, System.currentTimeMillis(), lastHardwareSteps, baseOffset, SystemClock.elapsedRealtime())
            )
            updateNotification(_liveStepsToday.value)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FIX MAX Motion Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous motion and step detection background service"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(steps: Long): Notification {
        val activity = _liveActivityState.value.name
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FIX MAX Step Tracking")
            .setContentText("Today: $steps steps • $activity")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
        _isServiceRunning.value = false
        sensorManager.unregisterListener(this)
        wakeLock?.let { if (it.isHeld) it.release() }
        handlerThread.quitSafely()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
