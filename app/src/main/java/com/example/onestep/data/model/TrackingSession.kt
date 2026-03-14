package com.example.onestep.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracking_sessions")
data class TrackingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long?,
    val totalSteps: Int,
    val durationInMillis: Long,
    val goal: Int = 10000
)
