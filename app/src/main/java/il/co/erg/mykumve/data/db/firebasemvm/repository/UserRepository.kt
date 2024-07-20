package il.co.erg.mykumve.data.db.firebasemvm.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

class UserRepository {

    private val TAG = UserRepository::class.java.simpleName
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun getAllUsers(): Flow<Resource<List<User>>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = usersCollection.get().await()
            val users = snapshot.documents.mapNotNull { it.toObject<User>() }
            emit(Resource.success(users))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    fun getUserById(id: String): Flow<Resource<User?>> = flow {
        emit(Resource.loading(null))
        try {
            val document = usersCollection.document(id).get().await()
            val user = document.toObject<User>()
            emit(Resource.success(user))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    fun getUserByEmail(email: String): Flow<Resource<User?>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = usersCollection.whereEqualTo("email", email).get().await()
            val user = snapshot.documents.firstOrNull()?.toObject<User>()
            emit(Resource.success(user))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    fun getUserByPhone(phone: String): Flow<Resource<User?>> = flow {
        emit(Resource.loading(null))
        try {
            val snapshot = usersCollection.whereEqualTo("phone", phone).get().await()
            val user = snapshot.documents.firstOrNull()?.toObject<User>()
            emit(Resource.success(user))
        } catch (e: Exception) {
            emit(Resource.error(e.message ?: "Unknown error", null))
        }
    }

    suspend fun insertUser(user: User): Resource<Void> {
        return safeCall {
            val document = usersCollection.document()
            user._id = document.id // Setting the private mutable field
            document.set(user).await()
            Resource.success(null)
        }
    }

    suspend fun updateUser(user: User): Resource<Void> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Resource.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user\n${e.cause.toString()}\n$e")
            Resource.error(e.message ?: "Failed to update user", null)
        }
    }

    suspend fun deleteUser(vararg user: User): Resource<Void> {
        return try {
            for (u in user) {
                usersCollection.document(u.id).delete().await()
            }
            Resource.success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user\n${e.cause.toString()}\n$e")
            Resource.error(e.message ?: "Failed to delete user", null)
        }
    }
}
