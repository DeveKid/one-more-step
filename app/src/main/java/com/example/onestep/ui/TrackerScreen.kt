package com.example.onestep.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onestep.data.model.TrackingSession
import com.example.onestep.ui.theme.Black
import com.example.onestep.ui.theme.Cyan
import com.example.onestep.ui.theme.NeonGreen
import com.example.onestep.ui.theme.MutedRed
import com.example.onestep.ui.theme.Gray
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.onestep.R

@Composable
fun TrackerScreen(
    currentSteps: Int,
    activeTime: Long,
    isTracking: Boolean,
    isPaused: Boolean,
    sensorExists: Boolean,
    dailyGoal: Int,
    sessions: List<TrackingSession>,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onUpdateGoal: (Int) -> Unit
) {
    var showGoalDialog by remember { mutableStateOf(false) }

    if (showGoalDialog) {
        GoalEditDialog(
            currentGoal = dailyGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { 
                onUpdateGoal(it)
                showGoalDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_walking_man),
                contentDescription = "App Logo",
                modifier = Modifier.size(32.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ONE MORE STEP",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        if (!sensorExists) {
            Text(
                text = "Hardware Step Counter not detected on this device.",
                color = MutedRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Hero Section: Step Count Circular Indicator
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            // Track (Background)
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxSize(),
                color = Color.DarkGray.copy(alpha = 0.3f),
                strokeWidth = 14.dp
            )
            // Progress
            CircularProgressIndicator(
                progress = (currentSteps.toFloat() / dailyGoal).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxSize(),
                color = when {
                    !isTracking -> Gray
                    isPaused -> Cyan.copy(alpha = 0.5f)
                    else -> NeonGreen
                },
                strokeWidth = 14.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showGoalDialog = true }
            ) {
                Text(
                    text = currentSteps.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Goal: $dailyGoal",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cyan,
                    modifier = Modifier.padding(4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatTime(activeTime),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isTracking && !isPaused) Cyan else Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isTracking) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("START", fontWeight = FontWeight.Bold, color = Black)
                }
            } else {
                Button(
                    onClick = { if (isPaused) onResume() else onPause() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) Cyan else Gray
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(if (isPaused) "RESUME" else "PAUSE", fontWeight = FontWeight.Bold, color = Black)
                }
                
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MutedRed),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("STOP", fontWeight = FontWeight.Bold, color = Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // History List
        Text(
            text = "RECENT SESSIONS",
            style = MaterialTheme.typography.labelLarge,
            color = Gray,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(sessions) { session ->
                SessionCard(session)
            }
        }
    }
}

@Composable
fun GoalEditDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Goal") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Steps") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { 
                text.toIntOrNull()?.let { onConfirm(it) }
            }) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
fun SessionCard(session: TrackingSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(session.startTime)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Text(
                    text = formatTime(session.durationInMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.totalSteps} steps",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Goal: ${session.goal}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray
                )
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val sec = (millis / 1000) % 60
    val min = (millis / (1000 * 60)) % 60
    val hr = (millis / (1000 * 60 * 60))
    return String.format("%02d:%02d:%02d", hr, min, sec)
}
