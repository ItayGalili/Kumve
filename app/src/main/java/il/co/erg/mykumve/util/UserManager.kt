package il.co.erg.mykumve.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import il.co.erg.mykumve.data.db.local_db.model.User
import com.google.gson.Gson

object UserManager {
    val TAG = UserManager::class.java.simpleName
    private const val PREFS_NAME = "user_prefs"
    private const val USER_KEY = "user"

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()

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
        return getUser() != null
    }

    fun clearUser() {
        Log.v(TAG, "Clearing user... ${getUser()?.firstName} [${getUser()?.id}]")
        val editor = prefs.edit()
        editor.remove(USER_KEY)
        editor.apply()
        Log.v(TAG, "User cleared... ${getUser()}")
    }
}
