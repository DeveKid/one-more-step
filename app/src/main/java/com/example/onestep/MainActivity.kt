package com.example.onestep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.onestep.ui.TrackerScreen
import com.example.onestep.ui.theme.OneStepTheme
import com.example.onestep.ui.viewmodel.TrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TrackerViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPermissions()

        setContent {
            OneStepTheme {
                val currentSteps by viewModel.currentSteps.collectAsState()
                val activeTime by viewModel.activeTime.collectAsState()
                val isTracking by viewModel.isTracking.collectAsState()
                val isPaused by viewModel.isPaused.collectAsState()
                val sensorExists by viewModel.sensorExists.collectAsState()
                val sessions by viewModel.sessions.collectAsState()
                val dailyGoal by viewModel.dailyGoal.collectAsState()

                TrackerScreen(
                    currentSteps = currentSteps,
                    activeTime = activeTime,
                    isTracking = isTracking,
                    isPaused = isPaused,
                    sensorExists = sensorExists,
                    dailyGoal = dailyGoal,
                    sessions = sessions,
                    onStart = { viewModel.startTracking() },
                    onStop = { viewModel.stopTracking() },
                    onPause = { viewModel.pauseTracking() },
                    onResume = { viewModel.resumeTracking() },
                    onUpdateGoal = { viewModel.updateDailyGoal(it) }
                )
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
