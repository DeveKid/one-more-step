package com.example.onestep.data.repository

import com.example.onestep.data.db.SessionDao
import com.example.onestep.data.model.TrackingSession
import kotlinx.coroutines.flow.Flow

class TrackingRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<TrackingSession>> = sessionDao.getAllSessions()

    suspend fun insert(session: TrackingSession) {
        sessionDao.insertSession(session)
    }
}
