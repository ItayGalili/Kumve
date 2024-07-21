package il.co.erg.mykumve.ui.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.databinding.RegisterBinding
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.ImagePickerUtil
import il.co.erg.mykumve.util.UserUtils.isValidPhoneNumber
import il.co.erg.mykumve.util.UserUtils.normalizePhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RegisterManager : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private lateinit var imagePickerUtil: ImagePickerUtil
    private lateinit var currentPhotoPath: String
    private var _binding: RegisterBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private var imageUri: String? = null

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterBinding.inflate(inflater, container, false)
        setupFieldValidation(binding.name, 3, getString(R.string.error_empty_name))
        setupFieldValidation(binding.passwordRegister, 6, getString(R.string.error_invalid_password))
        setupFieldValidation(binding.emailRegister, getString(R.string.error_invalid_email))
        setupFieldValidation(binding.PhoneRegister, getString(R.string.error_invalid_phone))

        // Initialize ImagePickerUtil
        imagePickerUtil = ImagePickerUtil(this, { uri ->
            binding.imagePersonRegister.setImageURI(uri)
            binding.imagePersonRegister.setPadding(0, 0, 0, 0)
        }, { success, downloadUrl ->
            if (success) {
                imageUri = downloadUrl
            } else {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        })

        binding.imagePersonRegister.setOnClickListener {
            showImagePickerDialog()
        }

        binding.RegisterBtn.setOnClickListener {
            if (validateInput()) {
                launch {
                    registerUser(it)
                }
            }
        }

        return binding.root
    }

    private fun showImagePickerDialog() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add Photo!")
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> {
                    imagePickerUtil.requestCaptureImagePermission()
                    dialog.dismiss()
                }
                items[item] == "Choose from Library" -> {
                    imagePickerUtil.pickImage()
                    dialog.dismiss()
                }
                items[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val fullName = binding.name.text.toString()
        val password = binding.passwordRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val phone = binding.PhoneRegister.text.toString()

        if (fullName.isBlank() || fullName.length < 3) {
            binding.name.error = getString(R.string.error_empty_name)
            isValid = false
        } else {
            binding.name.error = null
        }

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailRegister.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.emailRegister.error = null
        }

        if (password.isBlank() || password.length < 6) {
            binding.passwordRegister.error = getString(R.string.error_invalid_password)
            isValid = false
        } else {
            binding.passwordRegister.error = null
        }

        if (phone.isBlank() || !isValidPhoneNumber(phone)) {
            binding.PhoneRegister.error = getString(R.string.error_invalid_phone)
            isValid = false
        } else {
            binding.PhoneRegister.error = null
        }

        return isValid
    }

    private fun setupFieldValidation(editText: EditText, minLength: Int, errorMessage: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > before) {
                    if ((s?.length ?: 0) >= minLength) {
                        editText.error = null
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editText.text.length < minLength) {
                editText.error = errorMessage
            } else {
                editText.error = null
            }
        }
    }

    private fun setupFieldValidation(editText: EditText, errorMessage: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {}
        })

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && editText.text.isBlank()) {
                editText.error = errorMessage
            } else {
                editText.error = null
            }
        }
    }



    private fun registerUser(registerBtn: View?) {
        val fullName = binding.name.text.toString()
        val password = binding.passwordRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val phone = normalizePhoneNumber(binding.PhoneRegister.text.toString())
        val photo = imageUri

        val nameParts = fullName.split(" ")
        val firstName = nameParts.firstOrNull() ?: ""
        val surname = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else null

        val newUser = User(
            firstName = firstName,
            surname = surname,
            email = email,
            photo = photo,
            phone = phone
        )

        userViewModel.registerUser(newUser, password) { result ->
            launch(Dispatchers.Main) {
                if (isAdded) {
                    if (result.status == Status.SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            R.string.registration_successful,
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_registerManager_to_loginManager)
                    } else {
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
