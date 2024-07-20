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

class LoginManager : Fragment() {

    val TAG = LoginManager::class.java.simpleName
    private var _binding: LoginBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginBinding.inflate(inflater, container, false)

        binding.LoginBtn.setOnClickListener {
            val emailInput = binding.emailAd.text.toString()
            var password = binding.password.text.toString()
            if (isAdded) {
                loginUser(email, password) { isLoggedInUser ->
                    if (isLoggedInUser.success) {
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
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.fetchUserByEmail(email)
                userViewModel.userByEmail
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collectLatest { user ->
                        val resource = if (user != null) {
                            val passwordHash = EncryptionUtils.hashPassword(password, user.salt)
                            if (passwordHash == user.hashedPassword) {
                                UserManager.saveUser(user)
                                Resource.success(getString(R.string.login_successful))
                            } else {
                                Resource.error(getString(R.string.login_failed) + ": incorrect password")
                            }
                        } else {
                            Resource.error(getString(R.string.login_failed) + ": user not found")
                        }
                        callback(resource)
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
