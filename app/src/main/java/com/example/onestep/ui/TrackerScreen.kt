package com.example.onestep.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onestep.R
import com.example.onestep.data.model.TrackingSession
import com.example.onestep.ui.theme.*
import com.example.onestep.util.StepUtils
import com.example.onestep.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*

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
        HeaderSection()

        if (!sensorExists) {
            Text(
                text = stringResource(R.string.error_no_sensor),
                color = MutedRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Hero Section: Step Count Circular Indicator
        StepCounterSection(
            currentSteps = currentSteps,
            dailyGoal = dailyGoal,
            activeTime = activeTime,
            isTracking = isTracking,
            isPaused = isPaused,
            onClickGoal = { showGoalDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Live Metrics Row (Distance & Calories)
        MetricsSummaryRow(currentSteps = currentSteps)

        Spacer(modifier = Modifier.height(32.dp))

        // Control Buttons
        ControlButtonsRow(
            isTracking = isTracking,
            isPaused = isPaused,
            onStart = onStart,
            onStop = onStop,
            onPause = onPause,
            onResume = onResume
        )

        Spacer(modifier = Modifier.height(32.dp))

        HistorySection(sessions = sessions)
    }
}

@Composable
private fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_walking_man),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            colorFilter = ColorFilter.tint(Color.White)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.header_title),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StepCounterSection(
    currentSteps: Int,
    dailyGoal: Int,
    activeTime: Long,
    isTracking: Boolean,
    isPaused: Boolean,
    onClickGoal: () -> Unit
) {
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
            strokeCap = StrokeCap.Round
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onClickGoal() }
        ) {
            Text(
                text = currentSteps.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Text(
                text = stringResource(R.string.label_goal, dailyGoal),
                style = MaterialTheme.typography.labelLarge,
                color = Cyan,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = TimeUtils.formatDuration(activeTime),
                style = MaterialTheme.typography.titleMedium,
                color = if (isTracking && !isPaused) Cyan else Gray
            )
        }
    }
}

@Composable
private fun MetricsSummaryRow(currentSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MetricItem(
            label = stringResource(R.string.label_distance),
            value = stringResource(R.string.format_km, StepUtils.calculateDistanceKm(currentSteps))
        )
        MetricItem(
            label = stringResource(R.string.label_calories),
            value = stringResource(R.string.format_kcal, StepUtils.calculateCalories(currentSteps))
        )
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Gray)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ControlButtonsRow(
    isTracking: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
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
                Text(stringResource(R.string.btn_start), fontWeight = FontWeight.Bold, color = Black)
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
                val label = if (isPaused) stringResource(R.string.btn_resume) else stringResource(R.string.btn_pause)
                Text(label, fontWeight = FontWeight.Bold, color = Black)
            }
            
            Button(
                onClick = onStop,
                modifier = Modifier.weight(1f).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MutedRed),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(stringResource(R.string.btn_stop), fontWeight = FontWeight.Bold, color = Black)
            }
        }
    }
}

@Composable
private fun ColumnScope.HistorySection(sessions: List<TrackingSession>) {
    Text(
        text = stringResource(R.string.label_recent_sessions),
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

@Composable
fun GoalEditDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_goal_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.dialog_goal_label)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { 
                text.toIntOrNull()?.let { onConfirm(it) }
            }) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = TimeUtils.formatDuration(session.durationInMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.label_steps_count, session.totalSteps),
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.label_goal, session.goal),
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray
                    )
                }
            }
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = Color.DarkGray
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.format_km, StepUtils.calculateDistanceKm(session.totalSteps)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Cyan
                )
                Text(
                    text = stringResource(R.string.format_kcal, StepUtils.calculateCalories(session.totalSteps)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Cyan
                )
            }
        }
    }
}
