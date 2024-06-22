package com.example.mykumve.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.User
import com.example.mykumve.util.EncryptionUtils
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.launch

class UserViewModel (
    private val userRepository: UserRepository
) : ViewModel() {

    fun registerUser(
        firstName: String,
        surname: String?,
        email: String,
        password: String,
        photo: String?,
        description: String?,
        callback: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val existingUser = userRepository.getUserByEmail(email)
            if (existingUser != null) {
                callback(false)
            } else {
                val salt = EncryptionUtils.generateSalt()
                val passwordHashed = EncryptionUtils.hashPassword(password, salt)
                val newUser = User(firstName, surname, email, photo, description, passwordHashed, salt)
                userRepository.insertUser(newUser)
                UserManager.saveUser(newUser)
                callback(true)
            }
        }
    }
}
