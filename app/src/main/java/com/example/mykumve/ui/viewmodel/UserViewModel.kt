package com.example.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.User
import com.example.mykumve.util.EncryptionUtils
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.launch
import com.example.mykumve.util.Result
import kotlinx.coroutines.Dispatchers


class   UserViewModel (
    application: Application,
) : AndroidViewModel(application) {

    private var userRepository: UserRepository = UserRepository(application)
    suspend fun registerUser(
        firstName: String,
        surname: String?,
        email: String,
        password: String,
        photo: String?,
        description: String?,
        callback: (Result) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO){
            val existingUser = userRepository.getUserByEmail(email)?.value
            if (existingUser != null) {
                callback(Result(false, "User already registered.")) // Todo string
            } else {
                val salt = EncryptionUtils.generateSalt()
                val passwordHashed = EncryptionUtils.hashPassword(password, salt)
                val newUser = User(firstName, surname, email, photo, description, passwordHashed, salt)
                val result = userRepository.insertUser(newUser).await()
                if (result.success) {
                    UserManager.saveUser(newUser)
                }
                callback(result)
            }
        }
    }

    suspend fun updateUser(
        user: User,
        callback: (Result) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO){
            val result = userRepository.updateUser(user).await()
            if (result.success) {
                UserManager.saveUser(user)
            }
            callback(result)
        }
    }

    fun getUserByEmail(email: String): LiveData<User?>? {
        return userRepository.getUserByEmail(email)
    }
    fun getUserByPhone(phone: String): LiveData<User?>? {
        return userRepository.getUserByPhone(phone)
    }

    fun getUserById(id: Long): LiveData<User?>? {
        return userRepository.getUserById(id)
    }
    fun getAllUsers(): LiveData<List<User>>? {
        val users = userRepository.getAllUsers()
        Log.d("UserViewModel", "getAllUsers: ${users?.value}")
        return users
    }


}
