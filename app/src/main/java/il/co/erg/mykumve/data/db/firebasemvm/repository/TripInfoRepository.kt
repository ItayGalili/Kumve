package il.co.erg.mykumve.data.db.firebasemvm.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import il.co.erg.mykumve.data.db.model.Area
import il.co.erg.mykumve.data.db.model.SubArea
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.model.TripInvitation
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class TripInfoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tripInfoCollection = db.collection("trip_info")
    private val tripInvitationsCollection = db.collection("trip_invitations")
    private val areasCollection = db.collection("areas")

    fun getAllAreas(): Flow<Resource<List<Area>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = areasCollection.get().await()
            val areas = snapshot.documents.mapNotNull { it.toObject<Area>() }
            emit(Resource.success(areas))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    fun getSubAreasByAreaId(areaId: String): Flow<Resource<List<SubArea>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = areasCollection.document(areaId).collection("sub_areas").get().await()
            val subAreas = snapshot.documents.mapNotNull { it.toObject<SubArea>() }
            emit(Resource.success(subAreas))
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

    fun getTripInfoById(id: String): Flow<Resource<TripInfo?>> = flow {
        emit(Resource.loading(null))
        try {
            val document = tripInfoCollection.document(id).get().await()
            val tripInfo = document.toObject<TripInfo>()
            emit(Resource.success(tripInfo))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    suspend fun insertTripInfo(tripInfo: TripInfo): Resource<Void> {
        return try {
            val document = tripInfoCollection.document()
            tripInfo._id = document.id  // Set the internal mutable field
            document.set(tripInfo).await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to insert trip info", null)
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

    suspend fun deleteTripInfo(tripInfo: TripInfo): Resource<Void> {
        return try {
            tripInfoCollection.document(tripInfo.id).delete().await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to delete trip info", null)
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

    suspend fun insertTripInvitation(invitation: TripInvitation): Resource<Void> {
        return try {
            val document = tripInvitationsCollection.document()
            invitation._id = document.id  // Set the internal mutable field
            document.set(invitation).await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to insert trip invitation", null)
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

    suspend fun deleteTripInvitation(invitation: TripInvitation): Resource<Void> {
        return try {
            tripInvitationsCollection.document(invitation.id).delete().await()
            Resource.success(null)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to delete trip invitation", null)
        }
    }
}
