package com.example.mykumve.data.db.repository

import android.app.Application
import com.example.mykumve.data.db.local_db.TripDao
import com.example.mykumve.data.model.Trip
import androidx.lifecycle.LiveData
import com.example.mykumve.data.db.local_db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */


class TripRepository(application: Application): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var tripDao:TripDao? = null

    init {
        val db = AppDatabase.getDatabase(application)
        tripDao = db.tripDao()
    }

    fun getAllTrips(): LiveData<List<Trip>>? {
        return tripDao?.getAllTrips()
    }

    fun getTripById(id: Int): LiveData<Trip>? {
        return tripDao?.getTripById(id)
    }

    suspend fun insertTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao?.insertTrip(trip)
        }
    }

    suspend fun updateTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao?.updateTrip(trip)
        }
    }

    suspend fun deleteTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao?.deleteTrip(trip)
        }
    }

    fun getTripsByUserId(userId: Int): LiveData<List<Trip>>? {
        return tripDao?.getTripsByUserId(userId)
    }

}

