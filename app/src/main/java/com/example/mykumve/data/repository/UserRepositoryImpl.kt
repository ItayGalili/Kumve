package com.example.mykumve.data.repository

import com.example.mykumve.data.db.UserDao
import com.example.mykumve.data.model.User


/**
 * Implementation of UserRepository interface using Room.
 *
 * TODO: Add additional methods to handle complex user operations if needed.
 */
class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    override suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }
}
