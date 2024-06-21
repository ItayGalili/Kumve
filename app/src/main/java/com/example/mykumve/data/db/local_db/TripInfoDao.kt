package com.example.mykumve.data.db.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mykumve.data.model.TripInfo

/**
 * DAO interface for trip-related database operations.
 * Defines methods for querying, inserting, updating, and deleting trips.
 *
 * TODO: Add methods for complex queries if necessary.
 */

@Dao
interface TripInfoDao {

    @Query("SELECT * FROM trip_info WHERE id = :id")
    fun getTripInfoById(id: Int): LiveData<TripInfo>

    @Query("SELECT * FROM trip_info")
    fun getAllTripInfo(): LiveData<List<TripInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTripInfo(tripInfo: TripInfo)

    @Update
    fun updateTripInfo(tripInfo: TripInfo)

    @Delete
    fun deleteTripInfo(tripInfo: TripInfo)
}
