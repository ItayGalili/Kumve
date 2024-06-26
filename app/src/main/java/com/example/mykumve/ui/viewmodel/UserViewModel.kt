package com.example.mykumve.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.User
import com.example.mykumve.util.EncryptionUtils
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class   UserViewModel (
    application: Application,
) : AndroidViewModel(application) {

    private var userRepository: UserRepository = UserRepository(application)
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
                val result = userRepository.insertUser(newUser)
                if (result) {
                    UserManager.saveUser(newUser)
                }
                callback(result)
            }
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            userRepository.getUserByEmail(email)
        }
    }
    fun getUserById(id: Int): LiveData<User>? {
         // todo get should return live data
            return userRepository.getUserById(id)
    }

}
