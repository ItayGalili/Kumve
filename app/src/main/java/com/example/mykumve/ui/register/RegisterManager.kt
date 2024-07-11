package com.example.mykumve.ui.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.databinding.RegisterBinding
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.ImagePickerUtil
import com.example.mykumve.util.PATTERNS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext



class RegisterManager : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var _binding: RegisterBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var imagePickerUtil: ImagePickerUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterBinding.inflate(inflater, container, false)
        setupFieldValidation(binding.name, 3, getString(R.string.error_empty_name))
        setupFieldValidation(binding.passwordRegister, 6, getString(R.string.error_invalid_password))
        setupFieldValidation(binding.emailRegister, 6, getString(R.string.error_invalid_email))
        setupFieldValidation(binding.PhoneRegister, 10, getString(R.string.error_invalid_phone))

        imagePickerUtil = ImagePickerUtil(this) { uri ->
            binding.imagePersonRegister.setImageURI(uri)
        }

        binding.RegisterBtn.setOnClickListener {
            launch {
                if (validateInput()) {
                    registerUser(it)
                }
            }
        }
        binding.imagePersonRegister.setOnClickListener {
            imagePickerUtil.pickImage()
        }

        return binding.root
    }

    private suspend fun registerUser(registerBtn: View?) {
        val fullName = binding.name.text.toString()
        val password = binding.passwordRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val phone = _normalizePhoneNumber(binding.PhoneRegister.text.toString())
        val photo = imagePickerUtil.getImageUri()?.toString()

        val nameParts = fullName.split(" ")
        val firstName = nameParts.firstOrNull() ?: ""
        val surname = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else null
        userViewModel.registerUser(
            firstName,
            surname,
            email,
            password,
            photo,
            phone
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
        val phone = binding.PhoneRegister.text.toString()

        if (fullName.isBlank() || fullName.length < 3) {
            return false
        }

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }

        if (password.isBlank() || password.length < 6) {
            return false
        }

        if (phone.isBlank() || !isValidPhoneNumber(phone)) {
            return false
        }

        return true
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
// Regex to match an international phone number with a country code (+1 to +9999) optionally followed by a space,
        // then either 10 digits starting with 0 or 9 digits not starting with 0.
        val phoneRegex = Regex(PATTERNS.PHONE)
        return phoneRegex.matches(phone)
    }

    private fun setupFieldValidation(editText: EditText, minLength: Int, errorMessage: String) {
        var hasShownError = false

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > before) {
                    if ((s?.length ?: 0) >= minLength) {
                        editText.error = null
                        hasShownError = false
                    }
                } else if (hasShownError) {
                    if ((s?.length ?: 0) < minLength) {
                        editText.error = errorMessage
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (editText.text.length < minLength) {
                    editText.error = errorMessage
                    hasShownError = true
                } else {
                    editText.error = null
                    hasShownError = false
                }
            }
        }
    }

    fun _normalizePhoneNumber(phoneNumber: String): String {
        val matchResult = Regex(PATTERNS.PHONE).matchEntire(phoneNumber)

        if (matchResult != null) {
            // Extract the country code and the digits after it
            val countryCode = matchResult.groupValues[1]
            var digits = matchResult.groupValues[2]

            // Remove the leading zero if it's a 10-digit number starting with zero
            if (digits.startsWith('0')) {
                digits = digits.substring(1)
            }

            // Return the normalized phone number
            return "+$countryCode$digits"
        } else {
            throw IllegalArgumentException("Invalid format: Use '+[country code] [9-10 digits]'. Example: +1234567890 or +123 04567890.")
        }
    }
    private fun showToast(message: String) {
        launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
