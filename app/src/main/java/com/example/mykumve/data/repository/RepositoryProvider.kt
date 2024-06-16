package com.example.mykumve.data.repository

import com.example.mykumve.data.model.Trip

import android.content.Context
import com.example.mykumve.data.db.DatabaseProvider

/**
 * Singleton pattern for providing repository instances.
 *
 * TODO: Add additional repository providers as needed.
 */
object RepositoryProvider {

    private var userRepository: UserRepository? = null
    private var tripRepository: TripRepository? = null

    fun getUserRepository(context: Context): UserRepository {
        if (userRepository == null) {
            val userDao = DatabaseProvider.getDatabase(context).userDao()
            userRepository = UserRepositoryImpl(userDao)
        }
        return userRepository!!
    }

    fun getTripRepository(context: Context): TripRepository {
        if (tripRepository == null) {
            val tripDao = DatabaseProvider.getDatabase(context).tripDao()
            tripRepository = TripRepositoryImpl(tripDao)
        }
        return tripRepository!!
    }
}
