package com.example.mykumve.util

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
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(Base64.getDecoder().decode(salt))
        val hashedBytes = digest.digest(password.toByteArray())
        return Base64.getEncoder().encodeToString(hashedBytes)
    }
}
