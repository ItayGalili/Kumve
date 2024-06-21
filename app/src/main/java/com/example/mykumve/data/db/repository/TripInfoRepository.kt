package com.example.mykumve.data.db.repository

import com.example.mykumve.data.db.local_db.TripInfoDao
import androidx.lifecycle.LiveData
import com.example.mykumve.data.model.TripInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */

class TripInfoRepository(private val tripInfoDao: TripInfoDao) {

    fun getAllTripInfo(): LiveData<List<TripInfo>> {
        return tripInfoDao.getAllTripInfo()
    }

    fun getTripInfoById(id: Int): LiveData<TripInfo> {
        return tripInfoDao.getTripInfoById(id)
    }

    suspend fun insertTripInfo(tripInfo: TripInfo) {
        withContext(Dispatchers.IO) {
            tripInfoDao.insertTripInfo(tripInfo)
        }
    }

    suspend fun updateTripInfo(tripInfo: TripInfo) {
        withContext(Dispatchers.IO) {
            tripInfoDao.updateTripInfo(tripInfo)
        }
    }

    suspend fun deleteTripInfo(tripInfo: TripInfo) {
        withContext(Dispatchers.IO) {
            tripInfoDao.deleteTripInfo(tripInfo)
        }
    }
}


