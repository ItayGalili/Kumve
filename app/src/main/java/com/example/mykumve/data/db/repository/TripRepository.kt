package com.example.mykumve.data.db.repository

import com.example.mykumve.data.db.local_db.TripDao
import com.example.mykumve.data.model.Trip
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */


class TripRepository(private val tripDao: TripDao) {

    fun getAllTrips(): LiveData<List<Trip>> {
        return tripDao.getAllTrips()
    }

    fun getTripById(id: Int): LiveData<Trip> {
        return tripDao.getTripById(id)
    }

    suspend fun insertTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao.insertTrip(trip)
        }
    }

    suspend fun updateTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao.updateTrip(trip)
        }
    }

    suspend fun deleteTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao.deleteTrip(trip)
        }
    }

    fun getTripsByUserId(userId: Int): LiveData<List<Trip>> {
        return tripDao.getTripsByUserId(userId)
    }

}

