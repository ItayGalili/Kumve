package com.example.mykumve.data.db.repository

import android.app.Application
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.db.local_db.TripDao
import com.example.mykumve.data.db.local_db.TripInfoDao
import com.example.mykumve.data.model.Trip
import com.example.mykumve.util.DifficultyLevel


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */
class TripInfoRepository(application: Application){
    private var tripInfoDao: TripInfoDao?
    init {
        val db = AppDatabase.getDatabase(application.applicationContext)
        tripInfoDao = db.tripInfoDao()
    }

    fun getTripById(id: Int) =
        tripInfoDao?.getTripById(id)

    fun getTripByTripId(id: Int) =
        tripInfoDao?.getTripByTripId(id)
}

