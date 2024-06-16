package com.example.mykumve.data.repository

import com.example.mykumve.data.model.Trip

/**
 * Repository interface for trip-related data operations.
 * Abstracts data source for trip operations.
 *
 * TODO: Define methods for all required trip data operations.
 */
interface TripRepository {
    suspend fun getAllTrips(): List<Trip>
    suspend fun getTripById(id: Int): Trip?
    suspend fun insertTrip(trip: Trip): Long
    suspend fun updateTrip(trip: Trip)
    suspend fun deleteTrip(trip: Trip)
}
