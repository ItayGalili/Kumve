package com.example.mykumve.data.db.repository

import android.app.Application
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.db.local_db.UserDao
import com.example.mykumve.data.model.User


/**
 * Implementation of UserRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex user operations if needed.
 */
class UserRepository(application: Application){
    private var userDao: UserDao?
    init {
        val db = AppDatabase.getDatabase(application.applicationContext)
        userDao = db.userDao()
    }

    fun getUserById(id: Int) =
        userDao?.getUserById(id)//    fun getAllUsers() = userDao?.getAllUsers()

    fun getUserByEmail(email: String) =
        userDao?.getUserByEmail(email)

    fun addUser(user: User) = userDao?.insertUser(user)

    fun insertUser(user: User): Boolean {
        return try {
            userDao?.insertUser(user)
            true
        } catch (e: Exception) { // todo add log
            false
        }
    }
    fun deleteUser(user: User) = userDao?.deleteUser(user)
}