package com.example.mykumve.ui.login

import android.content.Context
import android.util.Base64
import com.example.mykumve.data.repository.RepositoryProvider
import com.example.mykumve.data.repository.UserRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginManagerHelper(private val context: Context) {

    private val userRepository: UserRepository by lazy {
        RepositoryProvider.getUserRepository(context)
    }

    fun loginUser(username: String, password: String): Boolean {
        var isValid = true
//        GlobalScope.launch {
//            val user = userRepository.getUserByUsername(username)
//            if (user != null) {
//                val passwordHash = hashPassword(password, user.salt)
//                if (passwordHash == user.passwordHash) {
//                    isValid = true
//                }
//            }
//        }
        return isValid
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(Base64.decode(salt, Base64.DEFAULT))
        val hashedBytes = digest.digest(password.toByteArray())
        return Base64.encodeToString(hashedBytes, Base64.DEFAULT)
    }
}
