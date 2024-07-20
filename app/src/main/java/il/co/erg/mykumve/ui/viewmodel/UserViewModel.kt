package il.co.erg.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import il.co.erg.mykumve.data.db.firebasemvm.repository.UserRepository
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.util.EncryptionUtils
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = UserViewModel::class.java.simpleName

    private val _operationResult = MutableSharedFlow<Resource<Void>>()
    val operationResult: SharedFlow<Resource<Void>> = _operationResult

    private val userRepository: UserRepository = UserRepository()

    private val _userByEmail = MutableStateFlow<User?>(null)
    val userByEmail: StateFlow<User?> get() = _userByEmail.asStateFlow()

    private val _userByPhone = MutableStateFlow<User?>(null)
    val userByPhone: StateFlow<User?> get() = _userByPhone.asStateFlow()

    private val _userById = MutableStateFlow<User?>(null)
    val userById: StateFlow<User?> get() = _userById.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> get() = _allUsers.asStateFlow()

    fun updatePassword(user: User, newPassword: String) {
        viewModelScope.launch {
            try {
                val updatedUser = user.copy(hashedPassword = newPassword)
                val result = userRepository.updateUser(updatedUser)
                _operationResult.emit(result)
            } catch (e: Exception) {
                _operationResult.emit(Resource.error(e.message ?: "Failed to update password", null))
            }
        }
    }

    fun registerUser(
        firstName: String,
        surname: String?,
        email: String,
        password: String,
        photo: String?,
        phone: String?,
        callback: (Resource<Void>) -> Unit
    ) {
        viewModelScope.launch {
            userRepository.getUserByEmail(email).collectLatest { resource ->
                val existingUser = resource.data
                if (existingUser != null) {
                    callback(Resource.error("User already registered.", null))
                } else {
                    val salt = EncryptionUtils.generateSalt()
                    val passwordHashed = EncryptionUtils.hashPassword(password, salt)
                    val newUser = User(firstName=firstName
                        , surname= surname
                        , email = email
                        , photo = photo
                        , phone = phone
                        , hashedPassword = passwordHashed
                        , salt = salt
                    )
                    val result = userRepository.insertUser(newUser)
                    if (result.status == Status.SUCCESS) {
                        UserManager.saveUser(newUser)
                    }
                    callback(result)
                }
            }
        }
    }

    fun updateUser(
        user: User,
        callback: (Resource<Void>) -> Unit
    ) {
        viewModelScope.launch {
            val result = userRepository.updateUser(user)
            if (result.status == Status.SUCCESS) {
                UserManager.saveUser(user)
            }
            callback(result)
        }
    }

    fun fetchUserByEmail(email: String) {
        viewModelScope.launch {
            userRepository.getUserByEmail(email).collectLatest { resource ->
                _userByEmail.emit(resource.data)
            }
        }
    }

    fun fetchUserByPhone(phone: String) {
        viewModelScope.launch {
            userRepository.getUserByPhone(phone).collectLatest { resource ->
                _userByPhone.emit(resource.data)
            }
        }
    }

    fun fetchUserById(id: String) {
        viewModelScope.launch {
            userRepository.getUserById(id).collectLatest { resource ->
                _userById.emit(resource.data)
            }
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collectLatest { resource ->
                _allUsers.emit(resource.data ?: emptyList())
            }
        }
    }

    fun observeUserByEmail(lifecycleOwner: androidx.lifecycle.LifecycleOwner, handleUserUpdate: (User?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userByEmail.collectLatest { user ->
                    handleUserUpdate(user)
                }
            }
        }
    }

    fun observeUserByPhone(lifecycleOwner: androidx.lifecycle.LifecycleOwner, handleUserUpdate: (User?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userByPhone.collectLatest { user ->
                    handleUserUpdate(user)
                }
            }
        }
    }

    fun observeUserById(lifecycleOwner: androidx.lifecycle.LifecycleOwner, handleUserUpdate: (User?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userById.collectLatest { user ->
                    handleUserUpdate(user)
                }
            }
        }
    }

    fun observeAllUsers(lifecycleOwner: androidx.lifecycle.LifecycleOwner, handleUsersUpdate: (List<User>) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                allUsers.collectLatest { users ->
                    handleUsersUpdate(users)
                }
            }
        }
    }
}
