package com.example.mykumve.ui.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.databinding.RegisterBinding
import com.example.mykumve.ui.viewmodel.UserViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Manages registration logic and operations.
 * Handles user registration.
 *
 * TODO: Implement more robust error handling and logging.
 */


class RegisterManager : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var _binding: RegisterBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    private var imageUri: Uri? = null
    val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.imagePersonRegister.setImageURI(it)
            requireActivity().contentResolver.takePersistableUriPermission(
                it!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imageUri = it
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterBinding.inflate(inflater, container, false)

        binding.RegisterBtn.setOnClickListener {
            launch {
                if (validateInput()) {
                    registerUser(it)
                }
            }
        }
        binding.imagePersonRegister.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        return binding.root
    }

    private suspend fun registerUser(registerBtn: View?) {
        val fullName = binding.name.text.toString()
        val password = binding.passwordRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val description = binding.descriptionRegister.text.toString()
        val photo = imageUri?.toString()

        val nameParts = fullName.split(" ")
        val firstName = nameParts.firstOrNull() ?: ""
        val surname = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else null
        userViewModel.registerUser(
            firstName,
            surname,
            email,
            password,
            photo,
            description
        ) { result ->
            launch(Dispatchers.Main) {
                if (result.success) {
                    Toast.makeText(
                        requireContext(),
                        R.string.registration_successful,
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registerManager_to_loginManager)
                } else {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                    // Todo descriptive error
                }
            }
        }
    }


    private fun validateInput(): Boolean {
        val fullName = binding.name.text.toString()
        val password = binding.passwordRegister.text.toString()
        val email = binding.emailRegister.text.toString()

        if (fullName.isBlank() || fullName.length < 3) {
            showToast(getString(R.string.error_empty_name))
            return false
        }

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast(getString(R.string.error_invalid_email))
            return false
        }

        if (password.isBlank() || password.length < 6) {
            showToast(getString(R.string.error_invalid_password))
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
