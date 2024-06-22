package com.example.mykumve.ui.login

import android.os.Bundle
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginManager : Fragment() {

    private var _binding: LoginBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginBinding.inflate(inflater, container, false)

        binding.LoginBtn.setOnClickListener {
            val username = binding.emailAd.text.toString()
            val password = binding.password.text.toString()

            runBlocking {
                if (loginUser(username, password)) {
                    Toast.makeText(requireContext(), R.string.login_successful, Toast.LENGTH_SHORT).show()

                    // todo - make sure user can't get back to login page
                    // Navigate to main screen
                    findNavController().navigate(R.id.action_loginManager_to_mainScreenManager)
                } else {
                    Toast.makeText(requireContext(), R.string.login_failed, Toast.LENGTH_SHORT).show()
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

    private suspend fun loginUser(username: String, password: String): Boolean {
        val user = userViewModel.getUserByEmail(username)
        return if (user != null) {
            val passwordHash = EncryptionUtils.hashPassword(password, user.salt)
            if (passwordHash == user.hashedPassword) {
                UserManager.saveUser(user)
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
