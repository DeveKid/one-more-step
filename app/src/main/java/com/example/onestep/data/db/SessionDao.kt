package com.example.onestep.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.onestep.data.model.TrackingSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrackingSession)

    @Query("SELECT * FROM tracking_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<TrackingSession>>

    @androidx.room.Delete
    suspend fun deleteSession(session: TrackingSession)

    @Query("DELETE FROM tracking_sessions")
    suspend fun deleteAll()
}
