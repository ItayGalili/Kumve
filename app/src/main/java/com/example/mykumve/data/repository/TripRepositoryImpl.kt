package com.example.mykumve.data.repository

import com.example.mykumve.data.db.TripDao
import com.example.mykumve.data.model.Trip

/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */
class TripRepositoryImpl(private val tripDao: TripDao) : TripRepository {

    override suspend fun getAllTrips(): List<Trip> {
        return tripDao.getAllTrips()
    }

    override suspend fun getTripById(id: Int): Trip? {
        return tripDao.getTripById(id)
    }

    override suspend fun insertTrip(trip: Trip): Long {
        return tripDao.insertTrip(trip)
    }

    override suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    override suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }
}
