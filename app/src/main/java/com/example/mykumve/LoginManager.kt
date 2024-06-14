package com.example.mykumve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mykumve.databinding.LoginBinding
import com.example.mykumve.databinding.MainScreenBinding

class LoginManager : Fragment() {
    private var _binding : LoginBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LoginBinding.inflate(inflater,container,false)

        binding.LoginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginManager_to_mainScreenManager)
        }

        binding.RegisterBtn.setOnClickListener {

            findNavController().navigate(R.id.action_loginManager_to_registerManager)

        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding.recycler.adapter = ItemAdapter(ItemManager.items)
        //binding.recycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}