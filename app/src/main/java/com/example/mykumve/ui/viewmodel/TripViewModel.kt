package com.example.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.TripInfoRepository
import com.example.mykumve.data.db.repository.TripRepository
import com.example.mykumve.data.model.Trip
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.data.model.User
import com.example.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.launch


class TripViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val TAG = TripViewModel::class.java.simpleName
    private var tripRepository: TripRepository = TripRepository(application)
    private var userRepository: UserRepository = UserRepository(application)
    private var tripInfoRepository: TripInfoRepository = TripInfoRepository(application)

    private val _trip = MutableLiveData<Trip>()
    val trip: LiveData<Trip> get() = _trip

    private val _tripInfo = MutableLiveData<TripInfo>()
    val tripInfo: LiveData<TripInfo> get() = _tripInfo

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> get() = _trips

    fun getTripById(id: Long): LiveData<Trip>? {
        return tripRepository.getTripById(id)
    }

    fun getTripInfoByTripId(tripId: Long) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.value
            trip?.tripInfoId?.let {
                _tripInfo.postValue(tripInfoRepository.getTripInfoById(it)?.value)
            }
        }
    }

    fun getTripInfoById(id: Long) {
        viewModelScope.launch {
            _tripInfo.postValue(tripInfoRepository.getTripInfoById(id)?.value)
        }
    }

    fun getAllTrips(): LiveData<List<Trip>>? {
        return tripRepository.getAllTrips()
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.insertTrip(trip)
        }
    }

    fun insertTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            tripInfoRepository.insertTripInfo(tripInfo)
        }
    }

    fun addTripWithInfo(trip: Trip, tripInfo: TripInfo) {
        viewModelScope.launch {
            try {
                tripRepository.insertTripWithInfo(trip, tripInfo)
            } catch (e: Exception) {
                Log.e("TripViewModel", "Failed to insert trip and trip info: ${e.message}")
            }
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip)
        }
    }

    fun updateTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripInfo.id)?.value ?: throw Exception("Trip not found")
            trip.tripInfoId = tripInfo.id
            tripRepository.updateTrip(trip)
            tripInfoRepository.updateTripInfo(tripInfo)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip)
        }
    }

    fun deleteTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            tripInfoRepository.deleteTripInfo(tripInfo)
        }
    }

    fun getTripsByUserId(userId: Long): LiveData<List<Trip>>? {
        return tripRepository.getTripsByUserId(userId)
    }

    fun getTripsByParticipantUserId(userId: Long): LiveData<List<Trip>> {
        val allTrips = tripRepository.getAllTrips()
        val filteredTrips = MutableLiveData<List<Trip>>()

        allTrips?.observeForever { trips ->
            filteredTrips.value = trips.filter { trip ->
                trip.participants?.any { it.id == userId } == true
            }
        }

        return filteredTrips
    }


    fun sendTripInvitation(tripId: Long, userId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitation = TripInvitation(tripId = tripId, userId = userId)
            val result = tripRepository.sendTripInvitation(invitation)
            callback(result)
        }
    }

    fun sendTripInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            invitation.status = TripInvitationStatus.PENDING
            val result = tripRepository.sendTripInvitation(invitation)
            callback(result)
        }
    }

    fun respondToTripInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val status = invitation.status
                Log.d(TAG, "Updating trip invitation with status: ${status}, TripId ${invitation.tripId}")
                tripRepository.updateTripInvitation(invitation)

                if (status == TripInvitationStatus.APPROVED) {
                    handleApprovedInvitation(invitation, callback)
                } else {
                    callback(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to respond to trip invitation: ${e.message}")
                callback(false)
            }
        }
    }

    //todo live data scope
    private fun handleApprovedInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        val tripLiveData = tripRepository.getTripById(invitation.tripId)
        val userLiveData = userRepository.getUserById(invitation.userId)

        val tripObserver = object : Observer<Trip?> {
            override fun onChanged(trip: Trip?) {
                if (trip != null) {
                    tripLiveData?.removeObserver(this)
                    addUserToTrip(trip, userLiveData, callback)
                }
            }
        }

        val userObserver = object : Observer<User?> {
            override fun onChanged(user: User?) {
                if (user != null) {
                    userLiveData?.removeObserver(this)
                    tripLiveData?.value?.let { trip ->
                        addUserToTrip(trip, userLiveData, callback)
                    }
                }
            }
        }

        tripLiveData?.observeForever(tripObserver)
        userLiveData?.observeForever(userObserver)
    }

    private fun addUserToTrip(trip: Trip, userLiveData: LiveData<User?>?, callback: (Boolean) -> Unit) {
        val user = userLiveData?.value
        if (trip != null && user != null) {
            trip.participants?.add(user)
            Log.d(TAG, "Adding user ${user.firstName} to trip participants ${trip.participants}")
            viewModelScope.launch {
                tripRepository.updateTrip(trip)
                callback(true)
            }
        } else {
            val error = "Failed to respond to trip invitation"
            Log.e(TAG, if (trip == null) error + ", Trip is null" else error + ", user is null")
            callback(false)
        }
    }

    // Method to get trip invitations by trip ID
    fun getTripInvitationsByTripId(tripId: Long): LiveData<List<TripInvitation>>? {
        return tripRepository.getTripInvitationsByTripId(tripId)
    }

    fun getTripInvitationsForUser(userId: Long): LiveData<List<TripInvitation>>? {
        return tripRepository.getTripInvitationsForUser(userId)
    }

    fun deleteTripInvitation(invitation: TripInvitation) {
        viewModelScope.launch{
            tripRepository.deleteTripInvitation(invitation)
        }
    }

    // Method to check if a user has pending invitations for a specific trip
    fun hasPendingInvitations(userId: Long, tripId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitations = tripRepository.getTripInvitationsByTripId(tripId)?.value
            val hasPending =
                invitations?.any { it.userId == userId && it.status == TripInvitationStatus.PENDING } == true
            callback(hasPending)
        }
    }


    // CRUD methods for equipments
    fun addEquipment(tripId: Long, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.value
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { add(equipment) }
                val result = tripRepository.updateTrip(trip)
                callback(true) // todo refactor async and  return true result
            } else {
                callback(false)
            }
        }
    }

    fun removeEquipment(tripId: Long, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.value
            if (trip != null) {
                trip.equipment =
                    trip.equipment.orEmpty().toMutableList().apply { remove(equipment) }
                val result = tripRepository.updateTrip(trip)
                callback(true) // todo refactor async and  return true result
            } else {
                callback(false)
            }
        }
    }

    fun updateEquipment(
        tripId: Long,
        oldEquipment: Equipment,
        newEquipment: Equipment,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.value
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply {
                    val index = indexOf(oldEquipment)
                    if (index != -1) {
                        set(index, newEquipment)
                    }
                }
                val result = tripRepository.updateTrip(trip)
                callback(true) // todo refactor async and  return true result
            } else {
                callback(false)
            }
        }

    }
}
