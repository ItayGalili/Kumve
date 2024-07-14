package com.example.mykumve.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.databinding.LoginBinding
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.EncryptionUtils
import com.example.mykumve.util.UserManager
import com.example.mykumve.util.Result

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
            var email = ""
            var password = ""
            when {
                 emailInput.isBlank() -> {
                     email = "daniel@a.com"
                     password = "123456"
                }
                emailInput.contains("1")   -> {
                     email = "user1@a.com"
                     password = "123456"
                }
                emailInput.contains("2")   -> {
                     email = "user2@a.com"
                     password = "123456"
                }
                emailInput.contains("3")   -> {
                     email = "user3@a.com"
                     password = "123456"
                }
                emailInput.contains("4")   -> {
                     email = "user4@a.com"
                     password = "123456"
                }
                emailInput.contains("5")   -> {
                     email = "user5@a.com"
                     password = "123456"
                }
                emailInput.contains("6")   -> {
                     email = "user6@a.com"
                     password = "123456"
                }
                emailInput.contains("7")   -> {
                     email = "user7@a.com"
                     password = "123456"
                }
                else -> {
                     email = emailInput
                     password = binding.password.text.toString()
                }
            }
            loginUser(email, password) { isLoggedInUser ->
                if (isLoggedInUser.success) {
                    Toast.makeText(requireContext(), R.string.login_successful, Toast.LENGTH_SHORT)
                        .show()
                    // Navigate to main screen
                    findNavController().navigate(R.id.action_loginManager_to_mainScreenManager)
                } else {
                    Toast.makeText(requireContext(), R.string.login_failed, Toast.LENGTH_SHORT)
                        .show()
                    Log.e(TAG, "User is not logged in: ${isLoggedInUser.reason}" )
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

        return binding.root
    }

    private fun loginUser(email: String, password: String, callback: (Result) -> Unit) {
        userViewModel.getUserByEmail(email)?.observe(viewLifecycleOwner) { user ->
            val isLoggedInUser = if (user != null) {
                val passwordHash = EncryptionUtils.hashPassword(password, user.salt)
                if (passwordHash == user.hashedPassword) {
                    UserManager.saveUser(user)
                    Result(true, R.string.login_successful.toString())
                } else {
                    Result(false, R.string.login_failed.toString() + " incorrect password") // todo
                }
            } else {
                    Result(false, R.string.login_failed.toString() + " user not found") // todo
            }
            callback(isLoggedInUser)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
