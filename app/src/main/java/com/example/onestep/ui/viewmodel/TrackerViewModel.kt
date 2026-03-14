package com.example.onestep.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.onestep.data.db.AppDatabase
import com.example.onestep.data.repository.TrackingRepository
import com.example.onestep.service.StepTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TrackingRepository
    val sessions = AppDatabase.getDatabase(application).sessionDao().getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSteps = StepTrackingService.currentSteps
    val activeTime = StepTrackingService.activeTime
    val isTracking = StepTrackingService.isTracking
    val isPaused = StepTrackingService.isPaused
    val sensorExists = StepTrackingService.sensorExists
    
    private val _dailyGoal = MutableStateFlow(10000)
    val dailyGoal = _dailyGoal.asStateFlow()

    init {
        val sessionDao = AppDatabase.getDatabase(application).sessionDao()
        repository = TrackingRepository(sessionDao)
    }

    fun startTracking() {
        val intent = Intent(getApplication(), StepTrackingService::class.java).apply {
            action = StepTrackingService.ACTION_START
        }
        getApplication<Application>().startService(intent)
    }

    fun stopTracking() {
        val intent = Intent(getApplication(), StepTrackingService::class.java).apply {
            action = StepTrackingService.ACTION_STOP
            putExtra(StepTrackingService.EXTRA_GOAL, _dailyGoal.value)
        }
        getApplication<Application>().startService(intent)
    }

    fun pauseTracking() {
        val intent = Intent(getApplication(), StepTrackingService::class.java).apply {
            action = StepTrackingService.ACTION_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    fun resumeTracking() {
        val intent = Intent(getApplication(), StepTrackingService::class.java).apply {
            action = StepTrackingService.ACTION_RESUME
        }
        getApplication<Application>().startService(intent)
    }

    fun updateDailyGoal(newGoal: Int) {
        _dailyGoal.value = newGoal
    }
}
