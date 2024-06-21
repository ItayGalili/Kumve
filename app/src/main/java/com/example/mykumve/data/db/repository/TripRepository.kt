package com.example.mykumve.data.db.repository

import android.app.Application
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.db.local_db.TripDao
import com.example.mykumve.data.model.Trip
import com.example.mykumve.util.DifficultyLevel


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */
class TripRepository(application: Application){
    private var tripDao: TripDao?
    init {
        val db = AppDatabase.getDatabase(application.applicationContext)
        tripDao = db.tripDao()
    }

    fun getAllTrips() = tripDao?.getAllTrips()
    fun getTripById(id: Int) =
        tripDao?.getTripById(id)
    fun getTripsByDifficulty(difficultyLevel: DifficultyLevel) =
        tripDao?.getTripsByDifficulty(difficultyLevel)
    fun addTrip(trip: Trip) = tripDao?.insertTrip(trip)
    fun updateTrip(trip: Trip) {
        tripDao?.updateTrip(trip)
    }
    fun deleteTrip(trip: Trip) = tripDao?.deleteTrip(trip)
}

