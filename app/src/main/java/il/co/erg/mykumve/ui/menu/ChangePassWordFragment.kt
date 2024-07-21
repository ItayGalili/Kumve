package il.co.erg.mykumve.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import il.co.erg.mykumve.R
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.launch

class ChangePasswordFragment : DialogFragment() {

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var previousPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnCancel: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_change_password, container, false)
        previousPasswordEditText = view.findViewById(R.id.previousPassword)
        newPasswordEditText = view.findViewById(R.id.newPassword)
        confirmNewPasswordEditText = view.findViewById(R.id.confirmNewPassword)
        btnChangePassword = view.findViewById(R.id.save_new_password)
        btnCancel = view.findViewById(R.id.keep_old_password)

        btnChangePassword.setOnClickListener {
            val previousPassword = previousPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val confirmNewPassword = confirmNewPasswordEditText.text.toString()
            changePassword(previousPassword, newPassword, confirmNewPassword)
        }
        btnCancel.setOnClickListener {
            dismiss()
        }
        return view
    }

    private fun changePassword(
        previousPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ) {
        if (UserManager.isLoggedIn()) {
            if (newPassword == confirmNewPassword) {
                if (newPassword != previousPassword) {
                    UserManager.getUser()?.let { user ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            val isOldPasswordCorrect =
                                UserManager.checkOldPassword(previousPassword)
                            if (isOldPasswordCorrect) {
                                if (newPassword.isBlank() || newPassword.length < 6) {
                                    Toast.makeText(
                                        requireContext(),
                                        R.string.error_invalid_password,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val updateSuccess = UserManager.updatePassword(newPassword)
                                    if (updateSuccess) {
                                        Toast.makeText(
                                            requireContext(),
                                            R.string.password_changed_successfully,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    Toast.makeText(
                                        requireContext(),
                                        "Password changed successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dismiss()
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Old password incorrect",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "New password and old password are the same",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "New passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "User not logged in",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
