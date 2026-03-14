package com.example.onestep.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.onestep.MainActivity
import com.example.onestep.R
import com.example.onestep.data.db.AppDatabase
import com.example.onestep.data.model.TrackingSession
import com.example.onestep.util.TimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StepTrackingService : LifecycleService(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    
    private var initialSteps = -1
    private var startTime = 0L
    
    private var timerJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "step_tracking_channel"
        private const val NOTIFICATION_ID = 1
        
        private val _currentSteps = MutableStateFlow(0)
        val currentSteps = _currentSteps.asStateFlow()

        private val _activeTime = MutableStateFlow(0L)
        val activeTime = _activeTime.asStateFlow()

        private val _isTracking = MutableStateFlow(false)
        val isTracking = _isTracking.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused = _isPaused.asStateFlow()

        private val _sensorExists = MutableStateFlow(true)
        val sensorExists = _sensorExists.asStateFlow()

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val EXTRA_GOAL = "EXTRA_GOAL"
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        _sensorExists.value = stepCounterSensor != null || stepDetectorSensor != null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> {
                val goal = intent.getIntExtra(EXTRA_GOAL, 10000)
                stopTracking(goal)
            }
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTracking() {
        if (_isTracking.value) return
        
        _isTracking.value = true
        _isPaused.value = false
        _currentSteps.value = 0
        initialSteps = -1
        startTime = System.currentTimeMillis()
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification(0, 0L))
        
        registerSensors()
        startTimer()
    }

    private fun pauseTracking() {
        if (!_isTracking.value || _isPaused.value) return
        _isPaused.value = true
        sensorManager.unregisterListener(this)
        timerJob?.cancel()
        updateNotification(_currentSteps.value, _activeTime.value)
    }

    private fun resumeTracking() {
        if (!_isTracking.value || !_isPaused.value) return
        _isPaused.value = false
        initialSteps = -1
        registerSensors()
        startTimer()
    }

    private fun registerSensors() {
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI, 5_000_000)
        }
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopTracking(goal: Int) {
        _isTracking.value = false
        _isPaused.value = false
        sensorManager.unregisterListener(this)
        timerJob?.cancel()
        
        val endTime = System.currentTimeMillis()
        val totalSteps = _currentSteps.value
        val duration = _activeTime.value

        lifecycleScope.launch {
            val session = TrackingSession(
                startTime = startTime,
                endTime = endTime,
                totalSteps = totalSteps,
                durationInMillis = duration,
                goal = goal
            )
            AppDatabase.getDatabase(applicationContext).sessionDao().insertSession(session)
            
            _currentSteps.value = 0
            _activeTime.value = 0L
            
            stopSelf()
        }
    }

    private var lastTimerUpdate = 0L
    private fun startTimer() {
        lastTimerUpdate = System.currentTimeMillis()
        timerJob = lifecycleScope.launch {
            while (_isTracking.value && !_isPaused.value) {
                _activeTime.value += System.currentTimeMillis() - lastTimerUpdate
                lastTimerUpdate = System.currentTimeMillis()
                updateNotification(_currentSteps.value, _activeTime.value)
                delay(1000)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_title),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(steps: Int, timeMillis: Long): Notification {
        val timeString = TimeUtils.formatDuration(timeMillis)
        val contentText = getString(R.string.notif_content, steps, timeString)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(steps: Int, timeMillis: Long) {
        val notification = createNotification(steps, timeMillis)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalStepsSinceReboot = event.values[0].toInt()
                if (initialSteps == -1) {
                    initialSteps = totalStepsSinceReboot
                }
                val calculated = totalStepsSinceReboot - initialSteps
                if (calculated > _currentSteps.value) {
                    _currentSteps.value = calculated
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    _currentSteps.value += 1
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
}
