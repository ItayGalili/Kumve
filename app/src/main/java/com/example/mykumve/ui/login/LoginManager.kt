package com.example.mykumve.ui.login

/**
 * Manages login logic and operations.
 * Handles user authentication.
 *
 * TODO: Implement more robust error handling and logging.
 */

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R

class LoginManager : Fragment() {

    private lateinit var loginManager: LoginManagerHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.login, container, false)
        loginManager = LoginManagerHelper(requireContext())

        view.findViewById<Button>(R.id.Login_Btn).setOnClickListener {
            val username = view.findViewById<EditText>(R.id.email_ad).text.toString()
            val password = view.findViewById<EditText>(R.id.password).text.toString()
            if (loginManager.loginUser(username, password)) {
                Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                // Navigate to main screen
                // Assuming you have a NavController setup
                 findNavController().navigate(R.id.action_loginManager_to_mainScreenManager)
            } else {
                Toast.makeText(requireContext(), "Login failed", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
