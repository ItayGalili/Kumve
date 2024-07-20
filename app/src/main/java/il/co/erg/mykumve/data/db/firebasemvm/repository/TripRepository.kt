package il.co.erg.mykumve.data.db.firebasemvm.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import il.co.erg.mykumve.data.db.model.Trip
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.model.TripInvitation
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class TripRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tripsCollection = db.collection("trips")
    private val tripInfoCollection = db.collection("trip_info")
    private val tripInvitationsCollection = db.collection("trip_invitations")

    fun getAllTrips(): Flow<Resource<List<Trip>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = tripsCollection.get().await()
            val trips = snapshot.documents.mapNotNull { it.toObject<Trip>() }
            emit(Resource.success(trips))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
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

    suspend fun insertTripWithInfo(trip: Trip, tripInfo: TripInfo, callback: (Resource<Void>) -> Unit) {
        try {
            val tripDocument = tripsCollection.document()
            trip._id = tripDocument.id  // Set the internal mutable field
            tripDocument.set(trip).await()

            tripInfo.tripId = trip.id
            val tripInfoDocument = tripInfoCollection.document()
            tripInfo._id = tripInfoDocument.id  // Set the internal mutable field
            tripInfoDocument.set(tripInfo).await()

            val updatedTrip = trip.copy(tripInfoId = tripInfo.id)
            tripDocument.set(updatedTrip).await()

            callback(Resource.success(null))
        } catch (e: Exception) {
            callback(Resource.error(e.message ?: "Failed to insert trip and trip info", null))
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
            tripInfoCollection.whereEqualTo("tripId", trip.id).get().await().documents.forEach {
                tripInfoCollection.document(it.id).delete().await()
            }
            tripInvitationsCollection.whereEqualTo("tripId", trip.id).get().await().documents.forEach {
                tripInvitationsCollection.document(it.id).delete().await()
            }
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to delete trip and related data", null)
        }
    }

    suspend fun sendTripInvitation(invitation: TripInvitation): Resource<Void> {
        return try {
            val document = tripInvitationsCollection.document()
            invitation._id = document.id  // Set the internal mutable field
            document.set(invitation).await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to send trip invitation", null)
        }
    }

    suspend fun updateTripInvitation(invitation: TripInvitation): Resource<Void> {
        return try {
            tripInvitationsCollection.document(invitation.id).set(invitation).await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to update trip invitation", null)
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

    fun getTripInvitationsForUser(userId: String): Flow<Resource<List<TripInvitation>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = tripInvitationsCollection.whereEqualTo("userId", userId).get().await()
            val invitations = snapshot.documents.mapNotNull { it.toObject<TripInvitation>() }
            emit(Resource.success(invitations))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
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
}
