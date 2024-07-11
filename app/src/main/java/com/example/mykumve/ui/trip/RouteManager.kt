package com.example.mykumve.ui.trip

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.data.data_classes.Point
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.databinding.RouteBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.DifficultyLevel

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
            if(verifyRouteForm()){
                saveTrip()
                sharedViewModel.resetNewTripState()
                findNavController().navigate(R.id.action_routeManager_to_mainScreenManager2)
            }
        }

        binding.MapBtn.setOnClickListener {
            findNavController().navigate(R.id.action_routeManager_to_mapFragment3)
        }

        return binding.root
    }

    private fun saveTrip() {
        sharedViewModel.trip.observe(viewLifecycleOwner, Observer { trip ->
            if (trip != null) {
                val title = ""
                val points = listOf<Point>()
                val areaId = -1
                val subAreaId = -1
                val description = ""
                val routeDescription = ""
                val difficulty = DifficultyLevel.UNSET
                val length = 0.0f
                val tags = listOf<String>()
                val isCircular = false
                val likes = 0
                val tripInfo = TripInfo(
                    title = title,
                    points = points,
                    areaId = areaId,
                    subAreaId = subAreaId,
                    description = description,
                    routeDescription = routeDescription,
                    difficulty = difficulty,
                    length = length,
                    tags = tags,
                    isCircular = isCircular,
                    likes = likes,
                )
                tripViewModel.addTripWithInfo(trip, tripInfo)
            }
        })
    }

    private fun verifyRouteForm(): Boolean {
        return true
        TODO("Not yet implemented")
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