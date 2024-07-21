package il.co.erg.mykumve.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import il.co.erg.mykumve.data.db.firebasemvm.repository.UserRepository
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.firebasemvm.util.safeCall
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = UserViewModel::class.java.simpleName

    private val _operationResult = MutableSharedFlow<Resource<Void>>()
    val operationResult: SharedFlow<Resource<Void>> = _operationResult

    private val userRepository: UserRepository = UserRepository()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _userByEmail = MutableStateFlow<User?>(null)
    val userByEmail: StateFlow<User?> get() = _userByEmail.asStateFlow()

    private val _userByPhone = MutableStateFlow<User?>(null)
    val userByPhone: StateFlow<User?> get() = _userByPhone.asStateFlow()

    private val _userById = MutableStateFlow<User?>(null)
    val userById: StateFlow<User?> get() = _userById.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> get() = _allUsers.asStateFlow()

//    fun updatePassword(user: User, newPassword: String) {
//        viewModelScope.launch {
//            try {
//                val updatedUser = user.copy(hashedPassword = newPassword)
//                val result = userRepository.updateUser(updatedUser)
//                _operationResult.emit(result)
//            } catch (e: Exception) {
//                _operationResult.emit(Resource.error(e.message ?: "Failed to update password", null))
//            }
//        }
//    }

    fun registerUser(
        user: User,
        password: String,
        callback: (Resource<User>) -> Unit
    ) {
        viewModelScope.launch {
            val result = safeCall {
                val userCredential = auth.createUserWithEmailAndPassword(user.email, password).await()
                val firebaseUser = userCredential.user
                if (firebaseUser != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName("${user.firstName} ${user.surname}")
                        .setPhotoUri(user.photo?.let { Uri.parse(it) })
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()

                    val newUser = user.copy(_id = firebaseUser.uid)
                    val insertResult = userRepository.insertUser(newUser)
                    if (insertResult.status == Status.SUCCESS) {
                        UserManager.saveUser(newUser)
                        Resource.success(newUser)
                    } else {
                        Resource.error("Failed to register user in Firestore", null)
                    }
                } else {
                    Resource.error("Failed to register user with FirebaseAuth", null)
                }
            }
            callback(result)
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

    fun fetchUserById(id: String): Flow<Resource<User?>> {
        return userRepository.getUserById(id)
    }

    fun fetchUsersByIds(ids: List<String>): Flow<Resource<List<User>>> = flow {
        userRepository.getUsersByIds(ids).collect {
            emit(it)
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collectLatest { resource ->
                _allUsers.emit(resource.data ?: emptyList())
            }
        }
    }

    fun observeUserByEmail(lifecycleOwner: LifecycleOwner, handleUserUpdate: (User?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userByEmail.collectLatest { user ->
                    handleUserUpdate(user)
                }
            }
        }
    }

    fun observeUserByPhone(lifecycleOwner: LifecycleOwner, handleUserUpdate: (User?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userByPhone.collectLatest { user ->
                    handleUserUpdate(user)
                }
            }
        }
    }

    fun observeUserById(lifecycleOwner: LifecycleOwner, handleUserUpdate: (User?) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userById.collectLatest { user ->
                    handleUserUpdate(user)
                }
            }
        }
    }

    fun observeAllUsers(lifecycleOwner: LifecycleOwner, handleUsersUpdate: (List<User>) -> Unit) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                allUsers.collectLatest { users ->
                    handleUsersUpdate(users)
                }
            }
        }
    }
}
