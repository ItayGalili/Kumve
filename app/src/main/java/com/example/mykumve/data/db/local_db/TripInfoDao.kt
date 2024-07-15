package com.example.mykumve.data.db.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mykumve.data.model.TripInfo

@Dao
interface TripInfoDao {

    @Query("SELECT * FROM trip_info WHERE id = :id")
    fun getTripInfoById(id: Long): LiveData<TripInfo>

    @Query("SELECT * FROM trip_info")
    fun getAllTripInfo(): LiveData<List<TripInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripInfo(tripInfo: TripInfo): Long

    @Update
    suspend fun updateTripInfo(tripInfo: TripInfo)

    @Delete
    suspend fun deleteTripInfo(tripInfo: TripInfo)
}
