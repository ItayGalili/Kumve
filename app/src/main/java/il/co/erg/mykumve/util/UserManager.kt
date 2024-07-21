package il.co.erg.mykumve.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import il.co.erg.mykumve.data.db.model.User
import kotlinx.coroutines.tasks.await

object UserManager {
    private const val TAG = "UserManager"
    private const val PREFS_NAME = "user_prefs"
    private const val USER_KEY = "user"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(user: User) {
        val editor = prefs.edit()
        val userJson = gson.toJson(user)
        editor.putString(USER_KEY, userJson)
        editor.apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString(USER_KEY, null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun clearUser() {
        Log.v(TAG, "Clearing user... ${getUser()?.firstName} [${getUser()?.id}]")
        val editor = prefs.edit()
        editor.remove(USER_KEY)
        editor.apply()
        auth.signOut()
        Log.v(TAG, "User cleared... ${getUser()}")
    }

    suspend fun checkOldPassword(previousPassword: String): Boolean {
        val firebaseUser = getFirebaseUser() ?: return false
        val user = getUser() ?: return false

        val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, previousPassword)
        return try {
            firebaseUser.reauthenticate(credential).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Reauthentication failed: ${e.message}")
            false
        }
    }

    suspend fun updatePassword(newPassword: String): Boolean {
        return try {
            // Update Firebase user password
            getFirebaseUser()?.updatePassword(newPassword)?.await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password: ${e.message}")
            false
        }
    }
}
