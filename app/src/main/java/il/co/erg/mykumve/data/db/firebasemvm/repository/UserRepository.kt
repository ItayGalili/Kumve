package il.co.erg.mykumve.data.db.firebasemvm.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.safeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val TAG = UserRepository::class.java.simpleName
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
            val document = usersCollection.document(user.id)
            document.set(user).await()
            Resource.success(null)
        }
    }

    suspend fun updateUser(user: User): Resource<Void> {
        return safeCall {
            usersCollection.document(user.id).set(user).await()
            Resource.success(null)
        }
    }

    suspend fun deleteUser(vararg user: User): Resource<Void> {
        return safeCall {
            for (u in user) {
                usersCollection.document(u.id).delete().await()
            }
            Resource.success(null)
        }
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Resource<User> {
        return safeCall {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw Exception("Google Sign-In failed")
            val user = User.fromFirebaseUser(firebaseUser)
            insertUser(user)
            Resource.success(user)
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { User.fromFirebaseUser(it) }
    }

    fun signOut() {
        auth.signOut()
    }
}
