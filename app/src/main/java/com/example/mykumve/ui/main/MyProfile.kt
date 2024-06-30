package com.example.mykumve.ui.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mykumve.databinding.MyProfilePageBinding
import com.example.mykumve.ui.register.RegisterManager

class MyProfile : Fragment() {

    private var _binding: MyProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var registerManager: RegisterManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MyProfilePageBinding.inflate(inflater, container, false)
        val view = binding.root
        registerManager = RegisterManager()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the imageUri from RegisterManager
        val imageUri = registerManager.getImageUri()

        // Set the imageUri to the ImageView in the layout
        binding.profilePic.setImageURI(imageUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}