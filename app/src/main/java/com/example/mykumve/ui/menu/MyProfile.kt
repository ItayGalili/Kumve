package com.example.mykumve.ui.menu

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mykumve.R
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.MyProfilePageBinding
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.ImagePickerUtil
import com.example.mykumve.util.UserManager
import com.example.mykumve.util.UserUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MyProfile : Fragment(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var _binding: MyProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var imagePickerUtil: ImagePickerUtil
    private lateinit var currentUser: User
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MyProfilePageBinding.inflate(inflater, container, false)
        val view = binding.root
        imagePickerUtil = ImagePickerUtil(this) { uri ->
            uri?.let {
                binding.profilePic.setImageURI(it)
                launch {
                    updateUserProfilePic(it)
                }
            }
        }

        if (UserManager.isLoggedIn()) {
            UserManager.getUser()?.let { user ->
                binding.profilePic.setImageURI(user.photo?.toUri())
                binding.profileUserFullNameTv.setText(UserUtils.getFullName(user))
                binding.profilePic.setOnLongClickListener {
                    imagePickerUtil.pickImage()
                    true
                }
            }
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            // You can navigate to the login screen or take appropriate action
        }

        return view
    }

    private suspend fun updateUserProfilePic(uri: Uri) {
        currentUser.photo = uri.toString()
        userViewModel.updateUser(currentUser) { result ->
            launch(Dispatchers.Main) {
                if (result.success) {
                    Toast.makeText(
                        requireContext(),
                        "Update successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(requireContext(), result.reason, Toast.LENGTH_SHORT).show()
                    // Todo descriptive error
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
