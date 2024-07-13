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
import com.example.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */


class TripRepository(application: Application,): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var tripDao:TripDao? = null
    private var userDao: UserDao? = null
    private var tripInvitationDao: TripInvitationDao? = null
    private var tripInfoDao: TripInfoDao? = null


    init {
        val db = AppDatabase.getDatabase(application)
        tripDao = db.tripDao()
        tripInvitationDao = db.tripInvitationDao()
        userDao = db.userDao()
        tripInfoDao = db.tripInfoDao()
    }

    fun getAllTrips(): LiveData<List<Trip>>? {
        return tripDao?.getAllTrips()
    }

    fun getTripById(id: Long): LiveData<Trip>? {
        return tripDao?.getTripById(id)
    }

    fun insertTrip(trip: Trip) {
        launch {
            tripDao?.insertTrip(trip)
        }
    }

    @Transaction
    suspend fun insertTripWithInfo(trip: Trip, tripInfo: TripInfo) {
        try {
            // Step 1: Insert Trip first without TripInfoId
            val tripId = tripDao?.insertTrip(trip)
            Log.d("TripRepository", "Inserted Trip with ID: $tripId")
            if (tripId != null){

                // Step 2: Insert TripInfo with the TripId from the inserted Trip
                val modifiedTripInfo = tripInfo.copy(tripId = tripId)
                val tripInfoId = tripInfoDao?.insertTripInfo(modifiedTripInfo)
                Log.d("TripRepository", "Inserted TripInfo with ID: $tripInfoId")

                // Step 3: Update the Trip with the newly created TripInfoId
                val updatedTrip = trip.copy(id = tripId, tripInfoId = tripInfoId)
                tripDao?.updateTrip(updatedTrip)
                Log.d("TripRepository", "Updated Trip with TripInfo ID: ${updatedTrip.tripInfoId}")

            }
        } catch (e: SQLiteConstraintException) {
            Log.e("TripRepository", "Foreign Key constraint failed: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("TripRepository", "Failed to insert trip and trip info: ${e.message}")
            throw e
        }
    }

    fun updateTrip(trip: Trip)  {
        tripDao?.updateTrip(trip)
    }

    fun deleteTrip(trip: Trip) {
        launch {
            tripDao?.deleteTripAndRelatedData(trip, tripInfoDao!!, tripInvitationDao!!)
        }
    }

    fun deleteTripInvitation(invitation: TripInvitation) {
        val trip = getTripById(invitation.tripId)?.value?.let { trip ->
            trip.invitations.removeAll { it.tripId == invitation.tripId }
            tripDao?.updateTrip(trip)
        }
    }

    fun getTripsByUserId(userId: Long): LiveData<List<Trip>>? {
        return tripDao?.getTripsByUserId(userId)
    }

    suspend fun sendTripInvitation(invitation: TripInvitation): Boolean {
        return try {
            tripInvitationDao?.insertTripInvitation(invitation)
            true
        } catch (e: Exception) { // todo add log
            false
        }
    }

    suspend fun respondToTripInvitation(invitation: TripInvitation): Boolean {
        return try {
            val status = invitation.status
            tripInvitationDao?.updateTripInvitation(invitation)
            if (status == TripInvitationStatus.APPROVED) {
                val trip = tripDao?.getTripById(invitation.tripId)?.value
                val user = userDao?.getUserById(invitation.userId)?.value
                if (trip != null && user != null) {
                    trip.participants?.add(user)
                    tripDao?.updateTrip(trip)
                }
            }
            true
        } catch (e: Exception) { // todo add log
            false
        }
    }

    fun getTripInvitationsByTripId(tripId: Long): LiveData<List<TripInvitation>>? {
        return tripInvitationDao?.getTripInvitationsByTripId(tripId)
    }

    fun getTripInvitationsForUser(userId: Long): LiveData<List<TripInvitation>>? {
        return tripInvitationDao?.getTripInvitationsForUser(userId)
    }


}

