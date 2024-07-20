package il.co.erg.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import il.co.erg.mykumve.data.db.model.Trip
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.model.TripInvitation
import il.co.erg.mykumve.data.db.firebasemvm.repository.TripInfoRepository
import il.co.erg.mykumve.data.db.firebasemvm.repository.TripRepository
import il.co.erg.mykumve.data.db.firebasemvm.repository.UserRepository
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.data_classes.Equipment
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.firebasemvm.util.safeCall
import il.co.erg.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TripViewModel(application: Application) : AndroidViewModel(application) {

    val TAG = TripViewModel::class.java.simpleName
    private val tripRepository: TripRepository = TripRepository()
    private val userRepository: UserRepository = UserRepository()
    private val tripInfoRepository: TripInfoRepository = TripInfoRepository()

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> get() = _trip.asStateFlow()

    private val _tripInfo = MutableStateFlow<TripInfo?>(null)
    val tripInfo: StateFlow<TripInfo?> get() = _tripInfo.asStateFlow()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    private val _tripsInfo = MutableStateFlow<List<TripInfo>>(emptyList())
    val trips: StateFlow<List<Trip>> get() = _trips.asStateFlow()
    val tripsInfo: StateFlow<List<TripInfo>> get() = _tripsInfo.asStateFlow()

    private val _tripInvitations = MutableStateFlow<List<TripInvitation>>(emptyList())
    val tripInvitations: StateFlow<List<TripInvitation>> get() = _tripInvitations.asStateFlow()

    private val _tripsWithInfo = MutableStateFlow<List<TripWithInfo>>(emptyList())
    val tripsWithInfo: StateFlow<List<TripWithInfo>> get() = _tripsWithInfo.asStateFlow()

    private val _operationResult = MutableSharedFlow<Resource<Void?>>()
    val operationResult: SharedFlow<Resource<Void?>> get() = _operationResult

    fun fetchTripsByParticipantUserIdWithInfo(userId: String) {
        viewModelScope.launch {
            try {
                _operationResult.emit(Resource.loading(null))

                val allTripsResource = tripRepository.getAllTrips().first { it.status != Status.LOADING } // Wait until it's not loading
                if (allTripsResource.status == Status.SUCCESS) {
                    val allTrips = allTripsResource.data ?: emptyList()
                    val tripsByParticipant = allTrips.filter { trip ->
                        trip.participants?.any { it.id == userId } == true
                    }

                    val tripsWithInfoList = tripsByParticipant.map { trip ->
                        val tripInfoResource = trip.tripInfoId?.let {
                            tripInfoRepository.getTripInfoById(it).first { info -> info.status != Status.LOADING }
                        }
                        val tripInfo = if (tripInfoResource?.status == Status.SUCCESS) tripInfoResource.data else null
                        TripWithInfo(trip, tripInfo)
                    }

                    _tripsWithInfo.emit(tripsWithInfoList)
                    _operationResult.emit(Resource.success(null))  // Emit success with null for Void?
                } else {
                    _operationResult.emit(Resource.error("Failed to fetch trips: ${allTripsResource.message}", null))
                }
            } catch (e: Exception) {
                _operationResult.emit(Resource.error("An error occurred: ${e.message}", null))
            }
        }
    }
    fun fetchTripById(id: String) {
        viewModelScope.launch {
            tripRepository.getTripById(id).collectLatest { resource ->
                _trip.value = resource.data
            }
        }
    }

    fun fetchTripInfoById(id: String) {
        viewModelScope.launch {
            tripInfoRepository.getTripInfoById(id).collectLatest { resource ->
                _tripInfo.value = resource.data
            }
        }
    }

    fun fetchAllTrips() {
        viewModelScope.launch {
            try {
                tripRepository.getAllTrips().collectLatest { resource ->
                    _trips.value = resource.data ?: emptyList()
                    _operationResult.emit(Resource.success(null))
                }
            } catch (e: Exception) {
                _operationResult.emit(Resource.error(e.message ?: "Failed to fetch trips", null))
            }
        }
    }

    fun fetchAllTripsInfo() {
        viewModelScope.launch {
            try {
                tripInfoRepository.getAllTripInfo().collectLatest { resource ->
                    _tripsInfo.value = resource.data ?: emptyList()
                    _operationResult.emit(Resource.success(null))
                }
            } catch (e: Exception) {
                _operationResult.emit(Resource.error(e.message ?: "Failed to fetch trips info", null))
            }
        }
    }

    fun observeTrips(lifecycleOwner: LifecycleOwner, handleTripUpdates: (Trip?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trip.collectLatest { trip ->
                    handleTripUpdates(trip)
                }
            }
        }
    }

    fun observeTripInfo(lifecycleOwner: LifecycleOwner, handleTripInfoUpdates: (TripInfo?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripInfo.collectLatest { tripInfo ->
                    handleTripInfoUpdates(tripInfo)
                }
            }
        }
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            val result = tripRepository.insertTrip(trip)
            _operationResult.emit(result)
        }
    }

    fun insertTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            val result = tripInfoRepository.insertTripInfo(tripInfo)
            _operationResult.emit(result)
        }
    }

    fun addTripWithInfo(trip: Trip, tripInfo: TripInfo, lifecycleOwner: LifecycleOwner) {
        Log.d(TAG, "Starting addTripWithInfo")

        lifecycleOwner.lifecycleScope.launch {
            try {
                tripRepository.insertTripWithInfo(trip, tripInfo) { result ->
                    if (result.status == Status.SUCCESS) {
                        val insertedTripId = trip.id
                        Log.d(TAG, "Inserted Trip ID: $insertedTripId")

                        val updatedTrip = trip.copy(_id = insertedTripId)
                        processAndSendUnsentInvitations(updatedTrip) { success ->
                            lifecycleOwner.lifecycleScope.launch {
                                if (success) {
                                    _operationResult.emit(Resource.success(null))
                                } else {
                                    _operationResult.emit(Resource.error("Failed to send some invitations", null))
                                }
                            }
                        }
                    } else {
                        lifecycleOwner.lifecycleScope.launch {
                            _operationResult.emit(Resource.error(result.message ?: "Failed to insert trip and trip info", null))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert trip and trip info: ${e.message}")
                lifecycleOwner.lifecycleScope.launch {
                    _operationResult.emit(Resource.error("Failed to insert trip and trip info: ${e.message}", null))
                }
            }
        }
    }

    fun updateTripWithInfo(trip: Trip, tripInfo: TripInfo, lifecycleOwner: LifecycleOwner) {
        Log.d(TAG, "Starting updateTripWithInfo")

        lifecycleOwner.lifecycleScope.launch {
            try {
                tripRepository.updateTripWithInfo(trip, tripInfo) { result ->
                    lifecycleOwner.lifecycleScope.launch {
                        _operationResult.emit(result)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update trip and trip info: ${e.message}")
                lifecycleOwner.lifecycleScope.launch {
                    _operationResult.emit(Resource.error("Failed to update trip and trip info: ${e.message}", null))
                }
            }
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            val result = tripRepository.updateTrip(trip)
            _operationResult.emit(result)
        }
    }

    fun updateTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            val result = tripInfoRepository.updateTripInfo(tripInfo)
            _operationResult.emit(result)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            try {
                val result = tripRepository.deleteTrip(trip)
                _operationResult.emit(result)
                fetchAllTrips() // Ensure the trip list is updated after deletion
            } catch (e: Exception) {
                _operationResult.emit(Resource.error("Failed to delete trip: ${e.message}", null))
            }
        }
    }

    fun deleteTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            val result = tripInfoRepository.deleteTripInfo(tripInfo)
            _operationResult.emit(result)
        }
    }

    fun fetchTripsByUserId(userId: String) {
        viewModelScope.launch {
            tripRepository.getTripsByUserId(userId).collectLatest { resource ->
                _trips.value = resource.data ?: emptyList()
            }
        }
    }

    fun sendTripInvitation(tripId: String, userId: String, callback: (Resource<Void>) -> Unit) {
        viewModelScope.launch {
            val invitation = TripInvitation(tripId = tripId, userId = userId)
            val result = tripRepository.sendTripInvitation(invitation)
            if (result.status == Status.SUCCESS) {
                val updateTripResult = updateTripWithInvitation(tripId, invitation)
                callback(updateTripResult)
            } else {
                callback(result)
            }
        }
    }

    private suspend fun updateTripWithInvitation(tripId: String, invitation: TripInvitation): Resource<Void> {
        return safeCall {
            val tripResource = tripRepository.getTripById(tripId).first()
            if (tripResource.status == Status.SUCCESS) {
                val trip = tripResource.data ?: return@safeCall Resource.error("Trip not found", null)
                trip.invitations.add(invitation)
                tripRepository.updateTrip(trip).also { updateResult ->
                    if (updateResult.status == Status.SUCCESS) {
                        Resource.success(null)
                    } else {
                        Resource.error("Failed to update trip with new invitation", null)
                    }
                }
            } else {
                Resource.error("Failed to fetch trip: ${tripResource.message}", null)
            }
        }
    }


    private fun processAndSendUnsentInvitations(trip: Trip, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            var success = true
            trip.invitations.filter { it.status == TripInvitationStatus.UNSENT }.forEach { invitation ->
                val updatedInvitation = invitation.copy(tripId = trip.id)
                sendTripInvitation(updatedInvitation) { result ->
                    if (!result) {
                        success = false
                    }
                }
            }
            callback(success)
        }
    }

    private fun sendTripInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            invitation.status = TripInvitationStatus.PENDING
            val result = tripRepository.sendTripInvitation(invitation)
            callback(result.status == Status.SUCCESS)
        }
    }

    fun respondToTripInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val status = invitation.status
                Log.d(TAG, "Updating trip invitation with status: $status, TripId ${invitation.tripId}")
                tripRepository.updateTripInvitation(invitation).let {
                    if (it.status == Status.SUCCESS && status == TripInvitationStatus.APPROVED) {
                        handleApprovedInvitation(invitation, callback)
                    } else {
                        callback(it.status == Status.SUCCESS)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to respond to trip invitation: ${e.message}")
                callback(false)
            }
        }
    }

    private fun handleApprovedInvitation(invitation: TripInvitation, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val tripResource = tripRepository.getTripById(invitation.tripId).firstOrNull()
            val userResource = userRepository.getUserById(invitation.userId).firstOrNull()

            if (tripResource?.status == Status.SUCCESS && userResource?.status == Status.SUCCESS) {
                val trip = tripResource.data
                val user = userResource.data
                if (trip != null && user != null) {
                    trip.participants?.add(user)
                    Log.d(TAG, "Adding user ${user.firstName} to trip participants ${trip.participants}")
                    tripRepository.updateTrip(trip).let {
                        callback(it.status == Status.SUCCESS)
                    }
                } else {
                    callback(false)
                }
            } else {
                callback(false)
            }
        }
    }

    fun fetchTripInvitationsByTripId(tripId: String) {
        viewModelScope.launch {
            tripRepository.getTripInvitationsByTripId(tripId).collectLatest { resource ->
                _tripInvitations.value = resource.data ?: emptyList()
            }
        }
    }

    fun fetchTripInvitationsForUser(userId: String) {
        viewModelScope.launch {
            tripRepository.getTripInvitationsForUser(userId).collectLatest { resource ->
                _tripInvitations.value = resource.data ?: emptyList()
            }
        }
    }

    fun deleteTripInvitation(invitation: TripInvitation) {
        viewModelScope.launch {
            val result = tripRepository.deleteTripInvitation(invitation)
            _operationResult.emit(result)
        }
    }

    fun hasPendingInvitations(userId: String, tripId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val invitations = tripRepository.getTripInvitationsByTripId(tripId).firstOrNull()?.data
            val hasPending = invitations?.any { it.userId == userId && it.status == TripInvitationStatus.PENDING } == true
            callback(hasPending)
        }
    }

    fun addEquipment(tripId: String, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId).firstOrNull()?.data
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { add(equipment) }
                tripRepository.updateTrip(trip).let {
                    callback(it.status == Status.SUCCESS)
                }
            } else {
                callback(false)
            }
        }
    }

    fun removeEquipment(tripId: String, equipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId).firstOrNull()?.data
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { remove(equipment) }
                tripRepository.updateTrip(trip).let {
                    callback(it.status == Status.SUCCESS)
                }
            } else {
                callback(false)
            }
        }
    }

    fun updateEquipment(tripId: String, oldEquipment: Equipment, newEquipment: Equipment, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId).firstOrNull()?.data
            if (trip != null) {
                trip.equipment = trip.equipment.orEmpty().toMutableList().apply {
                    val index = indexOf(oldEquipment)
                    if (index != -1) {
                        set(index, newEquipment)
                    }
                }
                tripRepository.updateTrip(trip).let {
                    callback(it.status == Status.SUCCESS)
                }
            } else {
                callback(false)
            }
        }
    }
}

data class TripWithInfo(
    var trip: Trip,
    var tripInfo: TripInfo?
)
