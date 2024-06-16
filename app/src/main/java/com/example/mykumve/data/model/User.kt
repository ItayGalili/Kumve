package com.example.mykumve.data.model

/**
 * Data class representing a user.
 * Contains user information including username, hashed password, and salt.
 *
 * TODO: Add any additional user-related fields if necessary.
 */
data class User(
    @PrimaryKey(auto-generate = true) val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val salt: String
)
