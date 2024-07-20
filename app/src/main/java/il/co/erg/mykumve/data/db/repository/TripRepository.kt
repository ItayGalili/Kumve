package il.co.erg.mykumve.data.db.repository

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import il.co.erg.mykumve.data.db.local_db.TripDao
import il.co.erg.mykumve.data.model.Trip
import androidx.room.Transaction
import il.co.erg.mykumve.data.db.local_db.AppDatabase
import il.co.erg.mykumve.data.db.local_db.TripInfoDao
import il.co.erg.mykumve.data.db.local_db.TripInvitationDao
import il.co.erg.mykumve.data.db.local_db.UserDao
import il.co.erg.mykumve.data.model.TripInfo
import il.co.erg.mykumve.data.model.TripInvitation
import il.co.erg.mykumve.util.Result
import kotlinx.coroutines.flow.Flow


class TripRepository(application: Application) {
    private var tripDao: TripDao? = null
    private var userDao: UserDao? = null
    private var tripInvitationDao: TripInvitationDao? = null
    private var tripInfoDao: TripInfoDao? = null
    val TAG = TripRepository::class.java.simpleName


    init {
        val db = AppDatabase.getDatabase(application)
        tripDao = db.tripDao()
        tripInvitationDao = db.tripInvitationDao()
        userDao = db.userDao()
        tripInfoDao = db.tripInfoDao()
    }

    fun getAllTrips(): Flow<List<Trip>>? {
        return tripDao?.getAllTrips()
    }
    fun getAllTripInfo(): Flow<List<TripInfo>>? {
        return tripInfoDao?.getAllTripInfo()
    }

    fun getTripById(id: Long): Flow<Trip>? {
        return tripDao?.getTripById(id)
    }

    suspend fun insertTrip(trip: Trip) {
        tripDao?.insertTrip(trip)
    }

    @Transaction
    suspend fun insertTripWithInfo(trip: Trip, tripInfo: TripInfo, callback: (Result) -> Unit) {
        try {
            // Step 1: Insert Trip first without TripInfoId
            val tripId = tripDao?.insertTrip(trip)
            Log.d(TAG, "Inserted Trip with ID: $tripId")
            if (tripId != null) {

                // Step 2: Insert TripInfo with the TripId from the inserted Trip
                val modifiedTripInfo = tripInfo.copy(tripId = tripId)
                val tripInfoId = tripInfoDao?.insertTripInfo(modifiedTripInfo)
                Log.d(TAG, "Inserted TripInfo with ID: $tripInfoId")

                // Step 3: Update the Trip with the newly created TripInfoId
                val updatedTrip = trip.copy(id = tripId, tripInfoId = tripInfoId)
                tripDao?.updateTrip(updatedTrip)
                Log.d(TAG, "Updated Trip with TripInfo ID: ${updatedTrip.tripInfoId}")

                callback(
                    Result(
                        success = true,
                        reason = "Trip and TripInfo inserted successfully",
                        data = mapOf("tripId" to tripId, "tripInfoId" to tripInfoId)
                    )
                )
            } else {
                callback(Result(false, "Failed to insert Trip"))
            }
        } catch (e: SQLiteConstraintException) {
            Log.e(TAG, "Foreign Key constraint failed: ${e.message}")
            callback(Result(false, "Foreign Key constraint failed: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert trip and trip info: ${e.message}")
            callback(Result(false, "Failed to insert trip and trip info: ${e.message}"))
        }
    }


    @Transaction
    suspend fun updateTripWithInfo(trip: Trip, tripInfo: TripInfo, callback: (Result) -> Unit) {
        var result: Result? = null
        try {
            Log.d(TAG, "Starting updateTripWithInfo")

            // Step 1: Update Trip first
            tripDao?.updateTrip(trip)
            Log.d(TAG, "Updated Trip with ID: ${trip.id}")

            // Step 2: Update TripInfo
            val modifiedTripInfo = tripInfo.copy(tripId = trip.id)
            tripInfoDao?.updateTripInfo(modifiedTripInfo)
            Log.d(TAG, "Updated TripInfo with ID: ${tripInfo.id}")

            result = Result(true, "Trip and TripInfo updated successfully")
        } catch (e: SQLiteConstraintException) {
            Log.e(TAG, "Foreign Key constraint failed: ${e.message}")
            result = Result(false, "Foreign Key constraint failed: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update trip and trip info: ${e.message}")
            result = Result(false, "Failed to update trip and trip info: ${e.message}")
        } finally {
            Log.d(TAG, "Completed updateTripWithInfo")
        }
        callback(result ?: Result(false, "General Error"))
    }




    suspend fun updateTrip(trip: Trip) {
        tripDao?.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao?.deleteTripAndRelatedData(trip, tripInfoDao!!, tripInvitationDao!!)
    }

    suspend fun deleteTripInvitation(invitation: TripInvitation) {
//        val trip = getTripById(invitation.tripId)?.value?.let { trip ->
//            trip.invitations.removeAll { it.tripId == invitation.tripId }
//            tripDao?.updateTrip(trip)
//        }
    }

    suspend fun sendTripInvitation(invitation: TripInvitation): Boolean {
        return try {
            tripInvitationDao?.insertTripInvitation(invitation)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send trip invitation: ${e.message}")
            false
        }
    }

    suspend fun updateTripInvitation(invitation: TripInvitation) {
        tripInvitationDao?.updateTripInvitation(invitation)
    }

    fun getTripInvitationsByTripId(tripId: Long): Flow<List<TripInvitation>>? {
        return tripInvitationDao?.getTripInvitationsByTripId(tripId)
    }

    fun getTripInvitationsForUser(userId: Long): Flow<List<TripInvitation>>? {
        return tripInvitationDao?.getTripInvitationsForUser(userId)
    }

    fun getTripsByUserId(userId: Long): Flow<List<Trip>>? {
        return tripDao?.getTripsByUserId(userId)
    }

}

