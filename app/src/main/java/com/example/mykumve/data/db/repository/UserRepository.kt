package com.example.mykumve.data.db.repository

import android.app.Application
import com.example.mykumve.data.db.local_db.AppDatabase
import com.example.mykumve.data.db.local_db.UserDao
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.User
import com.example.mykumve.util.DifficultyLevel


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

//    fun getAllUsers() = userDao?.getAllUsers()
    fun getUserById(id: Int) =
        userDao?.getUserById(id)

    fun addUser(user: User) = userDao?.insertUser(user)

    fun updateUser(user: User) {
        userDao?.updateUser(user)
    }
    fun deleteUser(user: User) = userDao?.deleteUser(user)
}