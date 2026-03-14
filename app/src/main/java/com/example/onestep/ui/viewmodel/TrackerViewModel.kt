package com.example.onestep.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.onestep.data.repository.TrackingRepository
import com.example.onestep.service.StepTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class TrackerViewModel(
    application: Application,
    private val repository: TrackingRepository
) : AndroidViewModel(application) {

    val sessions = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSteps = StepTrackingService.currentSteps
    val activeTime = StepTrackingService.activeTime
    val isTracking = StepTrackingService.isTracking
    val isPaused = StepTrackingService.isPaused
    val sensorExists = StepTrackingService.sensorExists
    
    private val _dailyGoal = MutableStateFlow(10000)
    val dailyGoal = _dailyGoal.asStateFlow()

    fun startTracking() {
        sendIntent(StepTrackingService.ACTION_START)
    }

    fun stopTracking() {
        val intent = Intent(getApplication(), StepTrackingService::class.java).apply {
            action = StepTrackingService.ACTION_STOP
            putExtra(StepTrackingService.EXTRA_GOAL, _dailyGoal.value)
        }
        getApplication<Application>().startService(intent)
    }

    fun pauseTracking() {
        sendIntent(StepTrackingService.ACTION_PAUSE)
    }

    fun resumeTracking() {
        sendIntent(StepTrackingService.ACTION_RESUME)
    }

    private fun sendIntent(actionString: String) {
        val intent = Intent(getApplication(), StepTrackingService::class.java).apply {
            action = actionString
        }
        getApplication<Application>().startService(intent)
    }

    fun updateDailyGoal(newGoal: Int) {
        _dailyGoal.value = newGoal
    }

    /**
     * Factory for creating [TrackerViewModel] with dependencies.
     */
    class Factory(
        private val application: Application,
        private val repository: TrackingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackerViewModel(application, repository) as T
        }
    }
}
