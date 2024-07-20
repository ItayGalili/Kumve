package il.co.erg.mykumve.data.db.repository

import android.app.Application
import android.util.Log
import il.co.erg.mykumve.data.db.local_db.AppDatabase
import il.co.erg.mykumve.data.db.local_db.UserDao
import il.co.erg.mykumve.data.db.local_db.model.User
import il.co.erg.mykumve.util.Result
import kotlinx.coroutines.flow.Flow


class UserRepository(application: Application) {

    val TAG = UserRepository::class.java.simpleName
    private var userDao: UserDao?

    init {
        val db = AppDatabase.getDatabase(application.applicationContext)
        userDao = db.userDao()
    }
    fun getAllUsers(): Flow<List<User>>? {
        val users = userDao?.getAllUsers()
        return users
    }

    fun getUserById(id: Long): Flow<User?>? =
        userDao?.getUserById(id)

    fun getUserByEmail(email: String): Flow<User?>? {
        return userDao?.getUserByEmail(email)
    }
    fun getUserByPhone(phone: String): Flow<User?>? {
        return userDao?.getUserByPhone(phone)
    }

    suspend fun insertUser(user: User): Result {
        var result = Result(false, "General error Occurred")
        try {
            var res = userDao?.insertUser(user)
            Log.d(TAG, "User created with id $res")
            result = Result(true, "User inserted successfully") // todo string
        } catch (e: Exception) {
            val reason = "Failed to insert user\n${e.cause.toString()}" //todo string
            Log.e(TAG, "$reason\n$e.")
            result = Result(false, reason)
        }
        return result // Return the result
    }

    suspend fun updateUser(user: User): Result {
        var result = Result(false, "General error Occurred")
        try {
            var res = userDao?.updateUser(user)
            Log.d(TAG, "User updated with id $res")
            result = Result(true, "User inserted successfully") // todo string
        } catch (e: Exception) {
            val reason = "Failed to insert user\n${e.cause.toString()}" //todo string
            Log.e(TAG, "$reason\n$e.")
            result = Result(false, reason)
        }
        return result // Return the result
    }
}