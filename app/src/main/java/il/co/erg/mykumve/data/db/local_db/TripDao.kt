package il.co.erg.mykumve.data.db.local_db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import il.co.erg.mykumve.data.model.Trip
import il.co.erg.mykumve.data.model.TripInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips WHERE id = :id")
    fun getTripById(id: Long): Flow<Trip>

    @Query("SELECT * FROM trips")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE user_id = :userId")
    fun getTripsByUserId(userId: Long): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Transaction
    suspend fun insertTripWithInfo(trip: Trip, tripInfo: TripInfo, tripInfoDao: TripInfoDao?) {
        val tripId = insertTrip(trip)
        val modifiedTripInfo = tripInfo.copy(tripId = tripId)
        tripInfoDao?.insertTripInfo(modifiedTripInfo)
    }

    @Update
    suspend fun updateTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Query("DELETE FROM trip_info WHERE trip_id = :tripId")
    suspend fun deleteTripInfoByTripId(tripId: Long)

    @Query("DELETE FROM trip_invitations WHERE tripId = :tripId")
    suspend fun deleteTripInvitationsByTripId(tripId: Long)

    @Transaction
    suspend fun deleteTripAndRelatedData(trip: Trip, tripInfoDao: TripInfoDao, tripInvitationDao: TripInvitationDao) {
        deleteTripInfoByTripId(trip.id)
        deleteTripInvitationsByTripId(trip.id)
        deleteTrip(trip)
    }

}
