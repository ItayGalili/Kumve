package il.co.erg.mykumve.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import il.co.erg.mykumve.data.db.model.User

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
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            User.fromFirebaseUser(firebaseUser).also { saveUser(it) }
        } else {
            val userJson = prefs.getString(USER_KEY, null)
            userJson?.let { gson.fromJson(it, User::class.java) }
        }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun clearUser() {
        Log.v(TAG, "Clearing user... ${getUser()?.firstName} [${getUser()?.id}]")
        val editor = prefs.edit()
        editor.remove(USER_KEY)
        editor.apply()
        auth.signOut()
        Log.v(TAG, "User cleared... ${getUser()}")
    }

    fun signOut() {
        clearUser()
        auth.signOut()
    }
}
