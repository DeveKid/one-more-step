package com.example.onestep

import android.app.Application
import com.example.onestep.data.db.AppDatabase
import com.example.onestep.data.repository.TrackingRepository

class OneStepApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TrackingRepository(database.sessionDao()) }
}
