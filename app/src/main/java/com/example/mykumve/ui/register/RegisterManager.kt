package com.example.mykumve.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mykumve.R

/**
 * Manages registration logic and operations.
 * Handles user registration.
 *
 * TODO: Implement more robust error handling and logging.
 */


class RegisterManager : Fragment() {

    private lateinit var registerManager: RegisterManagerHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.register, container, false)
        registerManager = RegisterManagerHelper(requireContext())

        view.findViewById<Button>(R.id.Register_Btn).setOnClickListener {
            val username = view.findViewById<EditText>(R.id.name).text.toString()
            val password = view.findViewById<EditText>(R.id.password_register).text.toString()
            registerManager.registerUser(username, password) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                    // Navigate to login screen
                    // Assuming you have a NavController setup
                    // findNavController().navigate(R.id.action_registerManager_to_loginManager)
                } else {
                    Toast.makeText(requireContext(), "User already exists", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
