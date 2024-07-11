package com.example.mykumve.ui.trip

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.data.model.Trip
import com.example.mykumve.databinding.RouteBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel

class RouteManager : Fragment() {
    private var _binding : RouteBinding? = null

    private val binding get() = _binding!!
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedTripViewModel: SharedTripViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    lateinit var trip : Trip

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RouteBinding.inflate(inflater,container,false)

        setupSpinners()

        binding.seve.setOnClickListener {
            //val bundle = bundleOf("title" to binding.itemTitle.text.toString(), "description" to binding.itemDescription.text.toString())
            sharedViewModel.resetNewTripState()

            findNavController().navigate(R.id.action_routeManager_to_mainScreenManager2)
        }

        binding.MapBtn.setOnClickListener {
            findNavController().navigate(R.id.action_routeManager_to_mapFragment3)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedTripViewModel.trip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                // Use the trip data here
                //binding.DifficultySpinner.setSelection(getDifficultyIndex(it.difficulty))
                //binding.AreaSpinner.setSelection(getAreaIndex(it.areaId))
                // Populate other UI elements with trip data

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSpinners() {
        // Load difficulty options from strings.xml
        val difficultyOptions = resources.getStringArray(R.array.difficulty_options)
        val difficultyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, difficultyOptions)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.DifficultySpinner.adapter = difficultyAdapter

        // Load area options from strings.xml
        val areaOptions = resources.getStringArray(R.array.area_options)
        val areaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, areaOptions)
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.AreaSpinner.adapter = areaAdapter
    }
}