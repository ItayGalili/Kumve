package il.co.erg.mykumve.data.db.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import il.co.erg.mykumve.data.db.local_db.model.TripInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface TripInfoDao {

    @Query("SELECT * FROM trip_info WHERE id = :id")
    fun getTripInfoById(id: Long): Flow<TripInfo>

    @Query("SELECT * FROM trip_info")
    fun getAllTripInfo(): Flow<List<TripInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripInfo(tripInfo: TripInfo): Long

    @Update
    suspend fun updateTripInfo(tripInfo: TripInfo)

    @Delete
    suspend fun deleteTripInfo(tripInfo: TripInfo)
}
