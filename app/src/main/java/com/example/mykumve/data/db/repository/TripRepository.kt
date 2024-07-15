package com.example.mykumve.data.db.repository

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.mykumve.data.db.local_db.TripDao
import com.example.mykumve.data.model.Trip
import androidx.lifecycle.LiveData
import androidx.room.Transaction
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.db.local_db.TripInfoDao
import com.example.mykumve.data.db.local_db.TripInvitationDao
import com.example.mykumve.data.db.local_db.UserDao
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.data.model.TripInvitation
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

    fun getTripById(id: Long): Flow<Trip>? {
        return tripDao?.getTripById(id)
    }

    suspend fun insertTrip(trip: Trip) {
        tripDao?.insertTrip(trip)
    }

    @Transaction
    suspend fun insertTripWithInfo(trip: Trip, tripInfo: TripInfo) {
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
            }
        } catch (e: SQLiteConstraintException) {
            Log.e(TAG, "Foreign Key constraint failed: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert trip and trip info: ${e.message}")
            throw e
        }
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

