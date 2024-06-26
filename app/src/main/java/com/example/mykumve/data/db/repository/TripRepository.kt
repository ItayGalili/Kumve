package com.example.mykumve.data.db.repository

import android.app.Application
import com.example.mykumve.data.db.local_db.TripDao
import com.example.mykumve.data.model.Trip
import androidx.lifecycle.LiveData
import com.example.mykumve.data.db.dao.TripInvitationDao
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.db.local_db.UserDao
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


/**
 * Implementation of TripRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex trip operations if needed.
 */


class TripRepository(application: Application): CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var tripDao:TripDao? = null
    private var userDao: UserDao? = null
    private var tripInvitationDao: TripInvitationDao? = null


    init {
        val db = AppDatabase.getDatabase(application)
        tripDao = db.tripDao()
        tripInvitationDao = db.tripInvitationDao()
        userDao = db.userDao()
    }

    fun getAllTrips(): LiveData<List<Trip>>? {
        return tripDao?.getAllTrips()
    }

    fun getTripById(id: Int): LiveData<Trip>? {
        return tripDao?.getTripById(id)
    }

    suspend fun insertTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao?.insertTrip(trip)
        }
    }

    suspend fun updateTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao?.updateTrip(trip)
        }
    }

    suspend fun deleteTrip(trip: Trip) {
        withContext(Dispatchers.IO) {
            tripDao?.deleteTrip(trip)
        }
    }

    fun getTripsByUserId(userId: Int): LiveData<List<Trip>>? {
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

    suspend fun respondToTripInvitation(invitation: TripInvitation, status: TripInvitationStatus): Boolean {
        return try {
            invitation.status = status
            tripInvitationDao?.updateTripInvitation(invitation)
            if (status == TripInvitationStatus.APPROVED) {
                val trip = tripDao?.getTripById(invitation.tripId)?.value
                val user = userDao?.getUserById(invitation.userId)
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

    suspend fun getTripInvitationsByTripId(tripId: Int): List<TripInvitation>? {
        return tripInvitationDao?.getTripInvitationsByTripId(tripId)
    }


}

