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
            var email = ""
            when {
                emailInput.isBlank() -> {
                    email = "daniel@a.com"
                    password = "123456"
                }
                emailInput.contains("1") -> {
                    email = "user1@a.com"
                    password = "123456"
                }
                emailInput.contains("2") -> {
                    email = "user2@a.com"
                    password = "123456"
                }
                emailInput.contains("3") -> {
                    email = "user3@a.com"
                    password = "123456"
                }
                emailInput.contains("4") -> {
                    email = "user4@a.com"
                    password = "123456"
                }
                emailInput.contains("5") -> {
                    email = "user5@a.com"
                    password = "123456"
                }
                emailInput.contains("6") -> {
                    email = "user6@a.com"
                    password = "123456"
                }
                emailInput.contains("7") -> {
                    email = "user7@a.com"
                    password = "123456"
                }
                else -> {
                    email = emailInput
                }
            }
            loginUser(email, password) { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        Toast.makeText(requireContext(), R.string.login_successful, Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginManager_to_mainScreenManager)
                    }
                    Status.ERROR -> {
                        Toast.makeText(requireContext(), R.string.login_failed, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "User is not logged in: ${resource.message}")
                    }
                    else -> {
                        // Handle loading state if necessary
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
                            callback(resource)
                        }
                } else {
                    callback(Resource.error(getString(R.string.login_failed) + ": authentication failed"))
                }
            } catch (e: Exception) {
                callback(Resource.error(e.message ?: "Authentication failed"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
