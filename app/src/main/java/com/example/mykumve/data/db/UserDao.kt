package com.example.mykumve.data.db

/**
 * DAO interface for user-related database operations.
 * Defines methods for querying and inserting users.
 *
 * TODO: Add methods for updating and deleting users if necessary.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    fun getUserByUsername(username: String): User?

    @Insert
    fun insertUser(user: User): Long
}
