package il.co.erg.mykumve.data.db.repository

import android.app.Application
import il.co.erg.mykumve.data.db.local_db.TripInfoDao
import il.co.erg.mykumve.data.db.local_db.AppDatabase
import il.co.erg.mykumve.data.db.local_db.AreaDao
import il.co.erg.mykumve.data.db.local_db.model.TripInfo
import kotlinx.coroutines.flow.Flow


class TripInfoRepository(application: Application) {

    private var tripInfoDao: TripInfoDao? = null
    private var areaDao: AreaDao? = null


    init {
        val db = AppDatabase.getDatabase(application)
        tripInfoDao = db.tripInfoDao()
        areaDao = db.areaDao()
    }

    fun getAllAreas() = areaDao?.getAllAreas()
    fun getSubAreasByAreaId(areaId: Int) = areaDao?.getSubAreasByAreaId(areaId)
    fun getAllTripInfo(): Flow<List<TripInfo>>? {
        return tripInfoDao?.getAllTripInfo()
    }

    fun getTripInfoById(id: Long): Flow<TripInfo>? {
        return tripInfoDao?.getTripInfoById(id)
    }

    suspend fun insertTripInfo(tripInfo: TripInfo) {
        tripInfoDao?.insertTripInfo(tripInfo)
    }

    suspend fun updateTripInfo(tripInfo: TripInfo) {
        tripInfoDao?.updateTripInfo(tripInfo)
    }

    suspend fun deleteTripInfo(tripInfo: TripInfo) {
        tripInfoDao?.deleteTripInfo(tripInfo)
    }
}


