package com.example.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.TripInfoRepository
import com.example.mykumve.data.db.repository.TripRepository
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TripViewModel(application: Application) : AndroidViewModel(application) {

    val TAG = TripViewModel::class.java.simpleName
    private val tripRepository: TripRepository = TripRepository(application)
    private val userRepository: UserRepository = UserRepository(application)
    private val tripInfoRepository: TripInfoRepository = TripInfoRepository(application)

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> get() = _trip.asStateFlow()

    private val _tripInfo = MutableStateFlow<TripInfo?>(null)
    val tripInfo: StateFlow<TripInfo?> get() = _tripInfo.asStateFlow()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> get() = _trips.asStateFlow()

    private val _tripInvitations = MutableStateFlow<List<TripInvitation>>(emptyList())
    val tripInvitations: StateFlow<List<TripInvitation>> get() = _tripInvitations.asStateFlow()

    private val _tripsWithInfo = MutableStateFlow<List<TripWithInfo>>(emptyList())
    val tripsWithInfo: StateFlow<List<TripWithInfo>> get() = _tripsWithInfo.asStateFlow()

    fun fetchTripsByParticipantUserIdWithInfo(userId: Long) {
        viewModelScope.launch {
            val allTrips = tripRepository.getAllTrips()
                ?.firstOrNull() ?: emptyList()
            val tripsByParticipant = allTrips.filter { trip ->
                trip.participants?.any { it.id == userId } == true
            }

            val tripsWithInfoList = tripsByParticipant.map { trip ->
                val tripInfo =
                    trip.tripInfoId?.let { tripInfoRepository.getTripInfoById(it)
                        ?.firstOrNull() }
                TripWithInfo(trip, tripInfo)
            }
            _tripsWithInfo.emit(tripsWithInfoList)
        }
    }


//    fun fetchTripsByParticipantUserIdWithInfo(userId: Long) {
//        viewModelScope.launch {
//            val allTrips = tripRepository.getAllTrips()
//                ?.firstOrNull() ?: emptyList()
//            val tripsByParticipant = allTrips.filter { trip ->
//                trip.participants?.any { it.id == userId } == true
//            }
//
//            val tripsWithInfoList = tripsByParticipant.map { trip ->
//                val tripInfo =
//                    trip.tripInfoId?.let { tripInfoRepository.getTripInfoById(it).firstOrNull() }
//                TripWithInfo(trip, tripInfo)
//            }
//            _tripsWithInfo.emit(tripsWithInfoList)
//        }
//    }

    fun fetchTripById(id: Long) {
        viewModelScope.launch {
            tripRepository.getTripById(id)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { trip ->
                    _trip.value = trip
                }
        }
    }

    fun fetchTripInfoByTripId(tripId: Long) {
        viewModelScope.launch {
            tripRepository.getTripById(tripId)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { trip ->
                    trip?.tripInfoId?.let { tripInfoId ->
                        tripInfoRepository.getTripInfoById(tripInfoId)
                            ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                            ?.collectLatest { tripInfo ->
                                _tripInfo.value = tripInfo
                            }
                    }
                }
        }
    }

    fun fetchTripInfoById(id: Long) {
        viewModelScope.launch {
            tripInfoRepository.getTripInfoById(id)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { tripInfo ->
                    _tripInfo.value = tripInfo
                }
        }
    }

    fun fetchAllTrips() {
        viewModelScope.launch {
            tripRepository.getAllTrips()
                ?.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                ?.collectLatest { trips ->
                    _trips.value = trips
                }
        }
    }

    // Other methods remain unchanged

    fun observeTrips(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripInfo.collectLatest { tripInfo ->
                    // Handle tripInfo updates here
                }
            }
        }
    }

    fun observeTripInfo(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trip.collectLatest { trip ->
                    // Handle trip updates here
                }
            }
        }
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
            val trip = tripRepository.getTripById(tripInfo.id)?.firstOrNull()
                ?: throw Exception("Trip not found")
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

    fun fetchTripsByUserId(userId: Long) {
        viewModelScope.launch {
            tripRepository.getTripsByUserId(userId)?.collectLatest { trips ->
                _trips.value = trips
            }
        }
    }

//    fun fetchTripsByParticipantUserId(userId: Long) {
//        viewModelScope.launch {
//            tripRepository.getAllTrips()?.collectLatest { allTrips ->
//                _trips.value = allTrips.filter { trip -> trip.participants?.any { it.id == userId } == true }
//            }
//        }
//    }

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

    private fun handleApprovedInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(invitation.tripId)?.firstOrNull()
            val user = userRepository.getUserById(invitation.userId)?.firstOrNull()

            if (trip != null && user != null) {
                trip.participants?.add(user)
                Log.d(TAG, "Adding user ${user.firstName} to trip participants ${trip.participants}")
                tripRepository.updateTrip(trip)
                callback(true)
            } else {
                val error = "Failed to respond to trip invitation"
                Log.e(TAG, if (trip == null) error + ", Trip is null" else error + ", user is null")
                callback(false)
            }
        }
    }

    fun fetchTripInvitationsByTripId(tripId: Long) {
        viewModelScope.launch {
            tripRepository.getTripInvitationsByTripId(tripId)?.collectLatest { invitations ->
                _tripInvitations.value = invitations
            }
        }
    }

    fun fetchTripInvitationsForUser(userId: Long) {
        viewModelScope.launch {
            tripRepository.getTripInvitationsForUser(userId)?.collectLatest { invitations ->
                _tripInvitations.value = invitations
            }
        }
    }

    fun deleteTripInvitation(invitation: TripInvitation) {
        viewModelScope.launch {
            tripRepository.deleteTripInvitation(invitation)
        }
    }

    fun hasPendingInvitations(userId: Long, tripId: Long, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitations = tripRepository.getTripInvitationsByTripId(tripId)?.firstOrNull()
            val hasPending = invitations?.any { it.userId == userId && it.status == TripInvitationStatus.PENDING } == true
            callback(hasPending)
        }
    }

    fun addEquipment(tripId: Long, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.firstOrNull()
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { add(equipment) }
                tripRepository.updateTrip(trip)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    fun removeEquipment(tripId: Long, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.firstOrNull()
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { remove(equipment) }
                tripRepository.updateTrip(trip)
                callback(true)
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
            val trip = tripRepository.getTripById(tripId)?.firstOrNull()
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply {
                    val index = indexOf(oldEquipment)
                    if (index != -1) {
                        set(index, newEquipment)
                    }
                }
                tripRepository.updateTrip(trip)
                callback(true)
            } else {
                callback(false)
            }
        }
    }
}

data class TripWithInfo(
    val trip: Trip,
    val tripInfo: TripInfo?
)
