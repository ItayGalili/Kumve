package il.co.erg.mykumve.data.db.firebasemvm.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import il.co.erg.mykumve.data.db.model.Trip
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.model.TripInvitation
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class TripRepository {
    val TAG = TripRepository::class.java.simpleName
    private val db = FirebaseFirestore.getInstance()
    private val tripsCollection = db.collection("trips")
    private val tripInfoCollection = db.collection("trip_info")
    private val tripInvitationsCollection = db.collection("trip_invitations")

    fun getAllTrips(): Flow<Resource<List<Trip>>> = flow {
        emit(Resource.loading(null))
        val result = safeCall {
            val snapshot = tripsCollection.get().await()
            if (snapshot != null && !snapshot.isEmpty) {
                val trips = snapshot.documents.mapNotNull { it.toObject<Trip>() }
                Resource.success(trips)
            } else {
                Resource.error("No trips found", emptyList())
            }
        }
        emit(result)
    }

    fun getAllTripInfo(): Flow<Resource<List<TripInfo>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = tripInfoCollection.get().await()
            val tripInfoList = snapshot.documents.mapNotNull { it.toObject<TripInfo>() }
            emit(Resource.success(tripInfoList))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    suspend fun updateTripWithInfo(trip: Trip, tripInfo: TripInfo, callback: (Resource<Void?>) -> Unit) {
        try {
            // Update the Trip
            tripsCollection.document(trip.id).set(trip).await()

            // Update the TripInfo
            tripInfoCollection.document(tripInfo.id).set(tripInfo).await()

            callback(Resource.success(null))
        } catch (e: Exception) {
            Log.e("TripRepository", "Failed to update trip and trip info: ${e.message}")
            callback(Resource.error("Failed to update trip and trip info: ${e.message}", null))
        }
    }

    suspend fun deleteTripInvitation(invitation: TripInvitation): Resource<Void?> {
        return try {
            tripInvitationsCollection.document(invitation.id).delete().await()
            Resource.success(null)
        } catch (e: Exception) {
            Log.e("TripRepository", "Failed to delete trip invitation: ${e.message}")
            Resource.error("Failed to delete trip invitation: ${e.message}", null)
        }
    }


    fun getTripById(id: String): Flow<Resource<Trip?>> = flow {
        emit(Resource.loading(null))
        try {
            val document = tripsCollection.document(id).get().await()
            val trip = document.toObject<Trip>()
            emit(Resource.success(trip))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    suspend fun insertTrip(trip: Trip): Resource<Void> {
        return try {
            val document = tripsCollection.document()
            trip._id = document.id  // Set the internal mutable field
            document.set(trip).await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to insert trip", null)
        }
    }

    suspend fun insertTripWithInfo(trip: Trip, tripInfo: TripInfo): Resource<Void> {
        return safeCall {
            val tripDocRef = tripsCollection.document()
            trip._id = tripDocRef.id

            FirebaseFirestore.getInstance().runTransaction { transaction ->
                transaction.set(tripDocRef, trip)
                val tripInfoDocRef = tripInfoCollection.document()
                tripInfo._id = tripInfoDocRef.id
                transaction.set(tripInfoDocRef, tripInfo)

                Resource.success(null)
            }.await()
        }
    }


    suspend fun updateTrip(trip: Trip): Resource<Void> {
        return try {
            tripsCollection.document(trip.id).set(trip).await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to update trip", null)
        }
    }

    suspend fun updateTripInfo(tripInfo: TripInfo): Resource<Void> {
        return try {
            tripInfoCollection.document(tripInfo.id).set(tripInfo).await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to update trip info", null)
        }
    }

    suspend fun deleteTrip(trip: Trip): Resource<Void> {
        return try {
            tripsCollection.document(trip.id).delete().await()
//            tripInfoCollection.whereEqualTo("tripId", trip.id).get().await().documents.forEach {
//                tripInfoCollection.document(it.id).delete().await()
//            }
            tripInvitationsCollection.whereEqualTo("tripId", trip.id).get().await().documents.forEach {
                tripInvitationsCollection.document(it.id).delete().await()
            }
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to delete trip and related data", null)
        }
    }

    suspend fun sendTripInvitation(invitation: TripInvitation): Resource<Void> {
        return safeCall {
            val tripDocRef = tripsCollection.document(invitation.tripId)
            val invitationDocRef = tripInvitationsCollection.document()
            invitation._id = invitationDocRef.id  // Set the internal mutable field

            FirebaseFirestore.getInstance().runTransaction { transaction ->
                val tripSnapshot = transaction.get(tripDocRef)
                if (!tripSnapshot.exists()) {
                    return@runTransaction Resource.error<Void>("Trip not found")
                }

                val trip = tripSnapshot.toObject(Trip::class.java) ?: return@runTransaction Resource.error<Void>("Trip deserialization error")
                trip.invitationIds.add(invitation.id)

                transaction.set(tripDocRef, trip)
                transaction.set(invitationDocRef, invitation)

                Resource.success(null)
            }.await()
        }
    }

    suspend fun updateTripInvitation(invitation: TripInvitation): Resource<Void> {
        return safeCall {
            getTripInvitationDocumentReference(invitation.id).set(invitation).await()
            Resource.success(null)
        }
    }

    fun getTripInvitationsByTripId(tripId: String): Flow<Resource<List<TripInvitation>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = tripInvitationsCollection.whereEqualTo("tripId", tripId).get().await()
            val invitations = snapshot.documents.mapNotNull { it.toObject<TripInvitation>() }
            emit(Resource.success(invitations))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    fun getTripInvitationById(invitationId: String): Flow<Resource<TripInvitation>> = flow {
        emit(Resource.loading(null))
        val result = safeCall {
            val document = tripInvitationsCollection.document(invitationId).get().await()
            val invitation = document.toObject<TripInvitation>()
            if (invitation != null) {
                Resource.success(invitation)
            } else {
                Resource.error("Invitation not found", null)
            }
        }
        emit(result)
    }


    fun getTripInvitationsForUser(userId: String): Flow<Resource<List<TripInvitation>>> = flow {
        emit(Resource.loading(null))
        val result = safeCall {
            val snapshot = tripInvitationsCollection.whereEqualTo("userId", userId).get().await()
            val invitations = snapshot.documents.mapNotNull { it.toObject<TripInvitation>() }
            Resource.success(invitations)
        }
        emit(result)
    }

    fun getTripsByUserId(userId: String): Flow<Resource<List<Trip>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = tripsCollection.whereEqualTo("userId", userId).get().await()
            val trips = snapshot.documents.mapNotNull { it.toObject<Trip>() }
            emit(Resource.success(trips))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    suspend fun checkTripInvitationExists(invitationId: String): Boolean {
        return try {
            val documentSnapshot = getTripInvitationDocumentReference(invitationId).get().await()
            val exists = documentSnapshot.exists()
            Log.d(TAG, "checkTripInvitationExists: Document $invitationId exists: $exists")
            exists
        } catch (e: Exception) {
            Log.e(TAG, "checkTripInvitationExists: Error checking existence of document $invitationId", e)
            false
        }
    }
    fun getTripDocumentReference(tripId: String) =
        FirebaseFirestore.getInstance().collection("trips").document(tripId)

    fun getTripInvitationDocumentReference(invitationId: String) =
        FirebaseFirestore.getInstance().collection("tripInvitations").document(invitationId)

}
