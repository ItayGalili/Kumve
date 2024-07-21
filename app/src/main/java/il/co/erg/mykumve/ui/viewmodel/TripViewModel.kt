package il.co.erg.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import il.co.erg.mykumve.data.db.model.Trip
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.model.TripInvitation
import il.co.erg.mykumve.data.db.firebasemvm.repository.TripInfoRepository
import il.co.erg.mykumve.data.db.firebasemvm.repository.TripRepository
import il.co.erg.mykumve.data.db.firebasemvm.repository.UserRepository
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.firebasemvm.util.safeCall
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.util.TripInvitationStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

                val allTripsResource = tripRepository.getAllTrips()
                    .first { it.status != Status.LOADING } // Wait until it's not loading
                if (allTripsResource.status == Status.SUCCESS) {
                    val allTrips = allTripsResource.data ?: emptyList()
                    val tripsByParticipant = allTrips.filter { trip ->
                        trip.participantIds?.any { it == userId } == true
                    }

                    val tripsWithInfoList = tripsByParticipant.map { trip ->
                        val tripInfoResource = trip.tripInfoId?.let {
                            tripInfoRepository.getTripInfoById(it)
                                .first { info -> info.status != Status.LOADING }
                        }
                        val tripInfo =
                            if (tripInfoResource?.status == Status.SUCCESS) tripInfoResource.data else null
                        TripWithInfo(trip, tripInfo)
                    }

                    _tripsWithInfo.emit(tripsWithInfoList)
                    _operationResult.emit(Resource.success(null))  // Emit success with null for Void?
                } else {
                    _operationResult.emit(
                        Resource.error(
                            "Failed to fetch trips: ${allTripsResource.message}",
                            null
                        )
                    )
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

//    fun observeTrips(lifecycleOwner: LifecycleOwner, handleTripUpdates: (Trip?) -> Unit) {
//        lifecycleOwner.lifecycleScope.launch {
//            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                trip.collectLatest { trip ->
//                    handleTripUpdates(trip)
//                }
//            }
//        }
//    }

//    fun observeTripInfo(lifecycleOwner: LifecycleOwner, handleTripInfoUpdates: (TripInfo?) -> Unit) {
//        lifecycleOwner.lifecycleScope.launch {
//            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                tripInfo.collectLatest { tripInfo ->
//                    handleTripInfoUpdates(tripInfo)
//                }
//            }
//        }
//    }

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
                val result = tripRepository.insertTripWithInfo(trip, tripInfo)
                if (result.status == Status.SUCCESS) {
                    val insertedTripId = trip.id
                    Log.d(TAG, "Inserted Trip ID: $insertedTripId")

                    val updatedTrip = trip.copy(_id = insertedTripId)
                    processAndSendUnsentInvitations(updatedTrip) { invitationResult ->
                        lifecycleOwner.lifecycleScope.launch {
                            if (invitationResult.status == Status.SUCCESS) {
                                _operationResult.emit(Resource.success(null))
                            } else {
                                _operationResult.emit(
                                    Resource.error(
                                        "Failed to send some invitations: ${invitationResult.message}",
                                        null
                                    )
                                )
                            }
                        }
                    }
                } else {
                    lifecycleOwner.lifecycleScope.launch {
                        _operationResult.emit(
                            Resource.error(
                                result.message ?: "Failed to insert trip and trip info", null
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert trip and trip info: ${e.message}")
                lifecycleOwner.lifecycleScope.launch {
                    _operationResult.emit(
                        Resource.error(
                            "Failed to insert trip and trip info: ${e.message}",
                            null
                        )
                    )
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
                    _operationResult.emit(
                        Resource.error(
                            "Failed to update trip and trip info: ${e.message}",
                            null
                        )
                    )
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

//    fun updateTripInfo(tripInfo: TripInfo) {
//        viewModelScope.launch {
//            val result = tripInfoRepository.updateTripInfo(tripInfo)
//            _operationResult.emit(result)
//        }
//    }

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

//    fun deleteTripInfo(tripInfo: TripInfo) {
//        viewModelScope.launch {
//            val result = tripInfoRepository.deleteTripInfo(tripInfo)
//            _operationResult.emit(result)
//        }
//    }
//
//    fun fetchTripsByUserId(userId: String) {
//        viewModelScope.launch {
//            tripRepository.getTripsByUserId(userId).collectLatest { resource ->
//                _trips.value = resource.data ?: emptyList()
//            }
//        }
//    }

//    fun sendTripInvitation(tripId: String, userId: String, callback: (Resource<Void>) -> Unit) {
//        viewModelScope.launch {
//            val invitation = TripInvitation(tripId = tripId, userId = userId)
//            val result = tripRepository.sendTripInvitation(invitation)
//            if (result.status == Status.SUCCESS) {
//                val invitationId = result.data
//                val updateResult = updateTripWithInvitation(tripId, invitationId)
//                callback(updateResult)
//            } else {
//                callback(Resource.error(result.message ?: "Failed to send invitation", null))
//            }
//        }
//    }

    private suspend fun updateTripWithInvitation(
        tripId: String,
        invitationId: String
    ): Resource<Void> {
        return safeCall {
            val tripResource = tripRepository.getTripById(tripId).first()
            if (tripResource.status == Status.SUCCESS) {
                val trip =
                    tripResource.data ?: return@safeCall Resource.error("Trip not found", null)
                trip.invitationIds.add(invitationId)
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


    private fun processAndSendUnsentInvitations(trip: Trip, callback: (Resource<Void>) -> Unit) {
        viewModelScope.launch {
            var success = true
            val unsentInvitations = trip.invitationIds.filter { invitationId ->
                val invitationResource = tripRepository.getTripInvitationById(invitationId).first()
                invitationResource.data?.status == TripInvitationStatus.UNSENT
            }

            for (invitationId in unsentInvitations) {
                val invitationResource = tripRepository.getTripInvitationById(invitationId).first()
                if (invitationResource.status == Status.SUCCESS) {
                    val invitation = invitationResource.data?.copy(tripId = trip.id)
                    if (invitation != null) {
                        sendTripInvitation(invitation) { result ->
                            if (result.status != Status.SUCCESS) {
                                success = false
                            }
                        }
                    }
                } else {
                    success = false
                }
            }

            if (success) {
                callback(Resource.success(null))
            } else {
                callback(Resource.error("Failed to send some invitations", null))
            }
        }
    }

    fun sendTripInvitation(invitation: TripInvitation, callback: (Resource<Void>) -> Unit) {
        viewModelScope.launch {
            val result = tripRepository.sendTripInvitation(invitation)
            callback(result)
        }
    }

    fun respondToTripInvitation(invitation: TripInvitation, callback: (Resource<Void>) -> Unit) {
        viewModelScope.launch {
            val result = safeCall {
                val status = invitation.status
                Log.d(TAG, "Updating trip invitation with status: $status, TripId ${invitation.tripId}")

                // First, check if the invitation exists
                val invitationExists = tripRepository.checkTripInvitationExists(invitation.id)
                if (invitationExists) {
                    Log.d(TAG, "Invitation found: ${invitation.id}")
                    if (status == TripInvitationStatus.APPROVED) {
                        handleApprovedInvitation(invitation)
                    } else {
                        tripRepository.updateTripInvitation(invitation)
                    }
                } else {
                    Log.e(TAG, "Invitation not found: ${invitation.id}")
                    Resource.error("Invitation not found: ${invitation.id}", null)
                }
            }
            callback(result)
        }
    }

    private suspend fun handleApprovedInvitation(invitation: TripInvitation): Resource<Void> {
        return safeCall {
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val tripRef = tripRepository.getTripDocumentReference(invitation.tripId)
                val tripSnapshot = transaction.get(tripRef)
                if (!tripSnapshot.exists()) {
                    throw IllegalStateException("Trip not found: ${invitation.tripId}")
                }
                val trip = tripSnapshot.toObject(Trip::class.java)
                    ?: throw IllegalStateException("Trip deserialization error")

                val userRef = userRepository.getUserDocumentReference(invitation.userId)
                val userSnapshot = transaction.get(userRef)
                if (!userSnapshot.exists()) {
                    throw IllegalStateException("User not found: ${invitation.userId}")
                }
                val user = userSnapshot.toObject(User::class.java)
                    ?: throw IllegalStateException("User deserialization error")

                // Add user ID to participant IDs
                trip.participantIds = trip.participantIds?.toMutableList() ?: mutableListOf()
                trip.participantIds?.add(user.id)

                // Update the trip
                transaction.set(tripRef, trip)

                // Update the invitation status
                transaction.update(tripRepository.getTripInvitationDocumentReference(invitation.id), "status", TripInvitationStatus.APPROVED)

                null
            }.await()

            Resource.success(null)
        }
    }


    fun fetchTripInvitationById(invitationId: String): TripInvitation? {
        var invitation: TripInvitation? = null
        viewModelScope.launch {
            val resource = tripRepository.getTripInvitationById(invitationId).first()
            if (resource.status == Status.SUCCESS) {
                invitation = resource.data
            }
        }
        return invitation
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

//    fun deleteTripInvitation(invitation: TripInvitation) {
//        viewModelScope.launch {
//            val result = tripRepository.deleteTripInvitation(invitation)
//            _operationResult.emit(result)
//        }
//    }
//
//    fun hasPendingInvitations(userId: String, tripId: String, callback: (Boolean) -> Unit) {
//        viewModelScope.launch {
//            val invitations = tripRepository.getTripInvitationsByTripId(tripId).firstOrNull()?.data
//            val hasPending = invitations?.any { it.userId == userId && it.status == TripInvitationStatus.PENDING } == true
//            callback(hasPending)
//        }
//    }
//
//    fun addEquipment(tripId: String, equipment: Equipment, callback: (Boolean) -> Unit) {
//        viewModelScope.launch {
//            val trip = tripRepository.getTripById(tripId).firstOrNull()?.data
//            if (trip != null) {
//                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { add(equipment) }
//                tripRepository.updateTrip(trip).let {
//                    callback(it.status == Status.SUCCESS)
//                }
//            } else {
//                callback(false)
//            }
//        }
//    }
//
//    fun removeEquipment(tripId: String, equipment: Equipment, callback: (Boolean) -> Unit) {
//        viewModelScope.launch {
//            val trip = tripRepository.getTripById(tripId).firstOrNull()?.data
//            if (trip != null) {
//                trip.equipment = trip.equipment.orEmpty().toMutableList().apply { remove(equipment) }
//                tripRepository.updateTrip(trip).let {
//                    callback(it.status == Status.SUCCESS)
//                }
//            } else {
//                callback(false)
//            }
//        }
//    }
//
//    fun updateEquipment(tripId: String, oldEquipment: Equipment, newEquipment: Equipment, callback: (Boolean) -> Unit) {
//        viewModelScope.launch {
//            val trip = tripRepository.getTripById(tripId).firstOrNull()?.data
//            if (trip != null) {
//                trip.equipment = trip.equipment.orEmpty().toMutableList().apply {
//                    val index = indexOf(oldEquipment)
//                    if (index != -1) {
//                        set(index, newEquipment)
//                    }
//                }
//                tripRepository.updateTrip(trip).let {
//                    callback(it.status == Status.SUCCESS)
//                }
//            } else {
//                callback(false)
//            }
//        }
//    }
}

data class TripWithInfo(
    var trip: Trip,
    var tripInfo: TripInfo?
)
