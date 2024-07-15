package com.example.mykumve.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.User
import com.example.mykumve.util.EncryptionUtils
import com.example.mykumve.util.Result
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private var userRepository: UserRepository = UserRepository(application)

    private val _userByEmail = MutableStateFlow<User?>(null)
    val userByEmail: StateFlow<User?> get() = _userByEmail.asStateFlow()

    private val _userByPhone = MutableStateFlow<User?>(null)
    val userByPhone: StateFlow<User?> get() = _userByPhone.asStateFlow()

    private val _userById = MutableStateFlow<User?>(null)
    val userById: StateFlow<User?> get() = _userById.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> get() = _allUsers.asStateFlow()

    fun registerUser(
        firstName: String,
        surname: String?,
        email: String,
        password: String,
        photo: String?,
        description: String?,
        callback: (Result) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingUser = userRepository.getUserByEmail(email)?.value
            if (existingUser != null) {
                callback(Result(false, "User already registered.")) // Todo string
            } else {
                val salt = EncryptionUtils.generateSalt()
                val passwordHashed = EncryptionUtils.hashPassword(password, salt)
                val newUser = User(firstName, surname, email, photo, description, passwordHashed, salt)
                val result = userRepository.insertUser(newUser)
                if (result.success) {
                    UserManager.saveUser(newUser)
                }
                callback(result)
            }
        }
    }

    fun updateUser(
        user: User,
        callback: (Result) -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.updateUser(user)
            if (result.success) {
                UserManager.saveUser(user)
            }
            callback(result)
        }
    }

    fun fetchUserByEmail(email: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByEmail(email)?.value
            _userByEmail.emit(user)
        }
    }

    fun fetchUserByPhone(phone: String) {
        viewModelScope.launch {
            val user = userRepository.getUserByPhone(phone)?.value
            _userByPhone.emit(user)
        }
    }

    fun fetchUserById(id: Long) {
        viewModelScope.launch {
            val user = userRepository.getUserById(id)?.value
            _userById.emit(user)
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            val users = userRepository.getAllUsers()?.value ?: emptyList()
            _allUsers.emit(users)
        }
    }
}
