package com.example.mykumve

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mykumve.databinding.TravelManagerViewBinding

class TravelManager : Fragment() {

    private var _binding : TravelManagerViewBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TravelManagerViewBinding.inflate(inflater, container, false)
        binding.doneBtn.setOnClickListener {

            //TODO: Update difficulty level, date and picture sections
            val item = Item(binding.nameTrip.text.toString(),
                binding.description.text.toString(),
                "hard", "14.6.24", "1")
            ItemManager.add(item)
            findNavController().navigate(R.id.action_travelManager_to_mainScreenManager)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}