package com.example.mykumve.data.db.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mykumve.data.model.Trip

import com.example.mykumve.util.DifficultyLevel

/**
 * DAO interface for trip-related database operations.
 * Defines methods for querying, inserting, updating, and deleting trips.
 *
 * TODO: Add methods for complex queries if necessary.
 */

@Dao
interface TripDao {

    @Query("SELECT * FROM trips WHERE id = :id")
    fun getTripById(id: Int): LiveData<Trip>

    @Query("SELECT * FROM trips")
    fun getAllTrips(): LiveData<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrip(trip: Trip)

    @Update
    fun updateTrip(trip: Trip)

    @Delete
    fun deleteTrip(trip: Trip)
}
