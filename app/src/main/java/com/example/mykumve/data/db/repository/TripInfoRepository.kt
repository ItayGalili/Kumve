package com.example.mykumve.data.db.repository

import android.app.Application
import com.example.mykumve.data.db.local_db.TripInfoDao
import androidx.lifecycle.LiveData
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.model.TripInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */

class TripInfoRepository(application: Application): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var tripInfoDao: TripInfoDao? = null

    init {
        val db = AppDatabase.getDatabase(application)
        tripInfoDao = db.tripInfoDao()
    }


    fun getAllTripInfo(): LiveData<List<TripInfo>>? {
        return tripInfoDao?.getAllTripInfo()
    }

    fun getTripInfoById(id: Int): LiveData<TripInfo>? {
        return tripInfoDao?.getTripInfoById(id)
    }

    fun insertTripInfo(tripInfo: TripInfo) {
        launch {
            tripInfoDao?.insertTripInfo(tripInfo)
        }
    }

    fun updateTripInfo(tripInfo: TripInfo) {
        launch {
            tripInfoDao?.updateTripInfo(tripInfo)
        }
    }

    fun deleteTripInfo(tripInfo: TripInfo) {
        launch {
            tripInfoDao?.deleteTripInfo(tripInfo)
        }
    }
}


