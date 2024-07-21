package il.co.erg.mykumve.ui.menu

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.databinding.MyProfilePageBinding
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.ImagePickerUtil
import il.co.erg.mykumve.util.UserManager
import il.co.erg.mykumve.util.UserUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MyProfile : Fragment(), CoroutineScope {
    val TAG = MyProfile::class.java.simpleName

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var _binding: MyProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerUtil: ImagePickerUtil
    private lateinit var currentUser: User
    private val userViewModel: UserViewModel by activityViewModels()
    private var imageUri: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyProfilePageBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize ImagePickerUtil
        imagePickerUtil = ImagePickerUtil(this,
            onImagePicked = { uri ->
                binding.profilePic.setImageURI(uri)
            },
            onImageUploadResult = { success, downloadUrl ->
                if (success && downloadUrl != null) {
                    updateUserProfilePic(downloadUrl.toUri())
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        )


        if (UserManager.isLoggedIn()) {
            UserManager.getUser()?.let { user ->
                currentUser = user
                binding.profilePic.setImageURI(user.photo?.toUri())
                binding.profileUserFullNameTv.setText(UserUtils.getFullName(user))
                binding.changeProfilePic.setOnClickListener {
                    showImagePickerDialog()
                }
                binding.profileEmail.setText(user.email)
                binding.profilePhoneNumber.setText(user.phone)
                binding.changePassword.setOnClickListener {
                    showChangePasswordDialog()
                }
            }
        } else {
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun showImagePickerDialog() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Update Profile Picture")
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

    private fun showChangePasswordDialog() {
        val dialogFragment = ChangePasswordFragment()
        dialogFragment.show(parentFragmentManager, "ChangePasswordFragment")
    }

    private fun updateUserProfilePic(uri: Uri) {
        var error = ""
        if (::currentUser.isInitialized) {
            currentUser.photo = uri.toString()
            userViewModel.updateUser(currentUser) { result ->
                launch(Dispatchers.Main) {
                    if (result.status == Status.SUCCESS) {
                        Toast.makeText(
                            requireContext(),
                            "Profile Picture Updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        error = "Failed to update profile picture. ${result.message}"
                    }
                }
            }
        } else {
            error = "Failed to update profile picture"
        }
        if (error.isNotEmpty()) {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            Log.e(TAG, error)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
