package com.example.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.UserRepository
import com.example.mykumve.data.model.User
import com.example.mykumve.util.EncryptionUtils
import com.example.mykumve.util.Result
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = UserViewModel::class.java.simpleName

    private val userRepository: UserRepository = UserRepository(application)

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
        viewModelScope.launch {
            userRepository.getUserByEmail(email)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { existingUser ->
                    if (existingUser != null) {
                        callback(Result(false, "User already registered.")) // Todo string
                    } else {
                        val salt = EncryptionUtils.generateSalt()
                        val passwordHashed = EncryptionUtils.hashPassword(password, salt)
                        val newUser =
                            User(firstName, surname, email, photo, description, passwordHashed, salt)
                        val result = userRepository.insertUser(newUser)
                        if (result.success) {
                            UserManager.saveUser(newUser)
                        }
                        callback(result)
                    }
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
            userRepository.getUserByEmail(email)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { user ->
                    _userByEmail.emit(user)
                }
        }
    }

    fun fetchUserByPhone(phone: String) {
        viewModelScope.launch {
            userRepository.getUserByPhone(phone)
                ?.distinctUntilChanged()
                ?.collectLatest { user ->
                    _userByPhone.emit(user)
                }
        }
    }

    fun fetchUserById(id: Long) {
        viewModelScope.launch {
            userRepository.getUserById(id)
                ?.stateIn(viewModelScope, SharingStarted.Lazily, null)
                ?.collectLatest { user ->
                    _userById.emit(user)
                }
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers()
                ?.distinctUntilChanged()  // Ensure only distinct values are processed
                ?.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
                ?.collectLatest { users ->
                    if (users.isNotEmpty()) {  // Only log when users list is not empty
                        _allUsers.emit(users)
                    }
                }
        }
    }

    fun observeUserByEmail(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userByEmail.collectLatest { user ->
                    // Handle user updates here
                }
            }
        }
    }

    fun observeUserByPhone(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userByPhone.collectLatest { user ->
                    // Handle user updates here
                }
            }
        }
    }

    fun observeUserById(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userById.collectLatest { user ->
                    // Handle user updates here
                }
            }
        }
    }

    fun observeAllUsers(lifecycleOwner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                allUsers.collectLatest { users ->
                    // Handle users updates here
                }
            }
        }
    }
}
