package com.example.mykumve.data.db.repository

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.db.local_db.UserDao
import com.example.mykumve.data.model.User
import com.example.mykumve.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext


/**
 * Implementation of UserRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex user operations if needed.
 */
class UserRepository(application: Application): CoroutineScope {

    val TAG = UserRepository::class.java.toString()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var userDao: UserDao?

    fun getAllUsers(): LiveData<List<User>>? {
        val users = userDao?.getAllUsers()
        Log.d("UserRepository", "getAllUsers: ${users?.value}")
        return users
    }
    init {
        val db = AppDatabase.getDatabase(application.applicationContext)
        userDao = db.userDao()
    }

    fun getUserById(id: Int): LiveData<User?>? =
        userDao?.getUserById(id)

    fun getUserByEmail(email: String): LiveData<User?>? {
        return userDao?.getUserByEmail(email)
    }
    fun getUserByPhone(phone: String): LiveData<User?>? {
        return userDao?.getUserByPhone(phone)
    }

    suspend fun insertUser(user: User): Deferred<Result> = coroutineScope {
        async {
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
            result // Return the result
        }
    }

    suspend fun updateUser(user: User): Deferred<Result> = coroutineScope {
        async {
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
            result // Return the result
        }
    }
}