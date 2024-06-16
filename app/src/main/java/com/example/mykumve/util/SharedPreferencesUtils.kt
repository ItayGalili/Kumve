package com.example.mykumve.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class for handling encrypted shared preferences.
 * Provides methods to save and retrieve secure data.
 *
 * TODO: Add methods to handle more types of data if needed.
 */
object SharedPreferencesUtils {

    /*fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "encrypted_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveLoginStatus(context: Context, username: String, password: String) {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        with(sharedPreferences.edit()) {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }

    fun getLoginStatus(context: Context): Pair<String, String>? {
        val sharedPreferences = getEncryptedSharedPreferences(context)
        val username = sharedPreferences.getString("username", null)
        val password = sharedPreferences.getString("password", null)
        return if (username != null && password != null) {
            Pair(username, password)
        } else {
            null
        }
    }*/
}
