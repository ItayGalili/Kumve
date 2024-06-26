package com.example.mykumve.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.data.db.repository.TripInfoRepository
import com.example.mykumve.data.db.repository.TripRepository
import com.example.mykumve.data.model.Trip
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.launch


class TripViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private var tripRepository: TripRepository = TripRepository(application)
    private var tripInfoRepository: TripInfoRepository = TripInfoRepository(application)

    private val _trip = MutableLiveData<Trip>()
    val trip: LiveData<Trip> get() = _trip

    private val _tripInfo = MutableLiveData<TripInfo>()
    val tripInfo: LiveData<TripInfo> get() = _tripInfo

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> get() = _trips

    fun getTripById(id: Int) {
        viewModelScope.launch {
            _trip.postValue(tripRepository.getTripById(id)?.value)
        }
    }

    fun getTripInfoByTripId(tripId: Int) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.value
            trip?.tripInfoId?.let {
                _tripInfo.postValue(tripInfoRepository.getTripInfoById(it)?.value)
            }
        }
    }

    fun getTripInfoById(id: Int) {
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

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip)
        }
    }

    fun updateTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
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

    fun getTripsByUserId(userId: Int): LiveData<List<Trip>>? {
        return tripRepository.getTripsByUserId(userId)
    }

    fun sendTripInvitation(tripId: Int, userId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitation = TripInvitation(tripId = tripId, userId = userId)
            val result = tripRepository.sendTripInvitation(invitation)
            callback(result)
        }
    }

    // Method to respond to a trip invitation
    fun respondToTripInvitation(
        tripId: Int,
        invitationId: Int,
        status: TripInvitationStatus,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val invitation =
                tripRepository.getTripInvitationsByTripId(tripId)?.value?.find { it.id == invitationId }
            if (invitation != null) {
                invitation.status = status
                val result = tripRepository.respondToTripInvitation(invitation)
                callback(result)
            } else {
                callback(false)
            }
        }
    }

    // Method to get trip invitations by trip ID
    suspend fun getTripInvitationsByTripId(tripId: Int): LiveData<List<TripInvitation>>? {
        return tripRepository.getTripInvitationsByTripId(tripId)
    }

    // Method to check if a user has pending invitations for a specific trip
    fun hasPendingInvitations(userId: Int, tripId: Int, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitations = tripRepository.getTripInvitationsByTripId(tripId)?.value
            val hasPending =
                invitations?.any { it.userId == userId && it.status == TripInvitationStatus.PENDING } == true
            callback(hasPending)
        }
    }


    // CRUD methods for equipments
    fun addEquipment(tripId: Int, equipment: String, callback: (Boolean) -> Unit) {
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

    fun removeEquipment(tripId: Int, equipment: String, callback: (Boolean) -> Unit) {
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
        tripId: Int,
        oldEquipment: String,
        newEquipment: String,
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
