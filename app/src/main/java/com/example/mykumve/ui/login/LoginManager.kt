package com.example.mykumve.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.databinding.LoginBinding

class LoginManager : Fragment() {

    private var _binding: LoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginManager: LoginManagerHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginBinding.inflate(inflater, container, false)
        loginManager = LoginManagerHelper(requireContext())

        binding.LoginBtn.setOnClickListener {
            val username = binding.emailAd.text.toString()
            val password = binding.password.text.toString()
            if (loginManager.loginUser(username, password)) {
                Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                // Navigate to main screen
                findNavController().navigate(R.id.action_loginManager_to_mainScreenManager)
            } else {
                Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.RegisterBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginManager_to_registerManager)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
