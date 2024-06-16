package com.example.mykumve.data.db

/**
 * DAO interface for trip-related database operations.
 * Defines methods for querying, inserting, updating, and deleting trips.
 *
 * TODO: Add methods for complex queries if necessary.
 */
@Dao
interface TripDao {
    @Query("SELECT * FROM trips")
    fun getAllTrips(): List<Trip>

    @Query("SELECT * FROM trips WHERE id = :id")
    fun getTripById(id: Int): Trip?

    @Insert
    fun insertTrip(trip: Trip): Long

    @Update
    fun updateTrip(trip: Trip)

    @Delete
    fun deleteTrip(trip: Trip)
}
