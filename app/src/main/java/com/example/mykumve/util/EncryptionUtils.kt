package com.example.mykumve.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utility class for encryption and hashing.
 * Provides methods for generating salts and hashing passwords.
 *
 * TODO: Add more utility methods if needed.
 */
object EncryptionUtils {

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(Base64.decode(salt, Base64.DEFAULT))
        val hashedBytes = digest.digest(password.toByteArray())
        return Base64.encodeToString(hashedBytes, Base64.DEFAULT)
    }
}
