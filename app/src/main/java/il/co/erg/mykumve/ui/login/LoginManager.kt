package il.co.erg.mykumve.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.databinding.LoginBinding
import il.co.erg.mykumve.ui.main.MainActivity.Companion.DEBUG_MODE
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.EncryptionUtils
import il.co.erg.mykumve.util.UserManager
import il.co.erg.mykumve.util.UserUtils.getFullName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginManager : Fragment() {

    val TAG = LoginManager::class.java.simpleName
    private var _binding: LoginBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginBinding.inflate(inflater, container, false)

        binding.LoginBtn.setOnClickListener {
            val emailInput = binding.emailAd.text.toString()
            var password = binding.password.text.toString()
            var email = when {
                emailInput.isBlank() -> "daniel@a.com"
                emailInput.contains("1") -> "user1@a.com"
                emailInput.contains("2") -> "user2@a.com"
                emailInput.contains("3") -> "user3@a.com"
                emailInput.contains("4") -> "user4@a.com"
                emailInput.contains("5") -> "user5@a.com"
                emailInput.contains("6") -> "user6@a.com"
                emailInput.contains("7") -> "user7@a.com"
                else -> emailInput
            }

            if (emailInput.isBlank()) {
                password = "123456"
            }

            userViewModel.loginUser(email, password)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            userViewModel.loginState.collect { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(requireContext(), R.string.login_successful, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginManager_to_mainScreenManager)
                    }
                    Status.ERROR -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "User is not logged in: ${resource.message}")
                    }
                    Status.LOADING -> {
                        // Optionally handle loading state, e.g., show a progress bar
                    }
                }
            }
        }

        binding.password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.LoginBtn.performClick()
                true
            } else {
                false
            }
        }

        binding.RegisterBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginManager_to_registerManager)
        }

        if (DEBUG_MODE) {
            logUsers()
        }
        return binding.root
    }

    private fun logUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.fetchAllUsers()
                userViewModel.allUsers
                    .filter { users -> users.isNotEmpty() }
                    .distinctUntilChanged()
                    .collectLatest { users ->
                        Log.d(TAG, "Found ${users.size} users.")
                        users.forEach { user ->
                            val userInfo = """
                            User ID ${user.id}:
                            Name: ${getFullName(user)}
                            Phone: ${user.phone}
                            Email: ${user.email}
                        """.trimIndent()
                            Log.d(TAG, userInfo)
                        }
                    }
            }
        }
    }

    private fun loginUser(email: String, password: String, callback: (Resource<String>) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            val loginState = MutableStateFlow<Resource<String>>(Resource.loading())

            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    userViewModel.fetchUserByEmail(email)
                    userViewModel.userByEmail
                        .filterNotNull()
                        .distinctUntilChanged()
                        .collectLatest { user ->
                            val resource = if (user != null) {
                                UserManager.saveUser(user)
                                Resource.success(getString(R.string.login_successful))
                            } else {
                                Resource.error(getString(R.string.login_failed) + ": user not found")
                            }
                            loginState.emit(resource)
                        }
                } else {
                    loginState.emit(Resource.error(getString(R.string.login_failed) + ": authentication failed"))
                }
            } catch (e: Exception) {
                loginState.emit(Resource.error(e.message ?: "Authentication failed"))
            }

            loginState.collectLatest { resource ->
                callback(resource)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
