package com.example.mykumve.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.mykumve.R
import com.example.mykumve.databinding.MyProfilePageBinding
import com.example.mykumve.ui.register.RegisterManager
import com.example.mykumve.util.UserManager

class MyProfile : Fragment() {

    private var _binding: MyProfilePageBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MyProfilePageBinding.inflate(inflater, container, false)
        val view = binding.root

        if (UserManager.isLoggedIn()) {
            var currentUser = UserManager.getUser()

            if (currentUser != null) {
                binding.profilePic.setImageURI(currentUser.photo?.toUri())
            }

        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            // You can navigate to the login screen or take appropriate action
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}