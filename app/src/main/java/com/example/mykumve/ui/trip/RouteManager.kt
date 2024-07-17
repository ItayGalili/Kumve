package com.example.mykumve.ui.trip

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.data.data_classes.Point
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.databinding.RouteBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.DifficultyLevel
import com.example.mykumve.util.TripInfoUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RouteManager : Fragment() {
    private var _binding : RouteBinding? = null
    val TAG = RouteManager::class.java.simpleName

    private val binding get() = _binding!!
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedTripViewModel: SharedTripViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = RouteBinding.inflate(inflater,container,false)

        setupSpinners()

        binding.seve.setOnClickListener {
            if(verifyRouteForm() and saveTrip()){
                sharedViewModel.isEditingExistingTrip = false
                sharedViewModel.resetNewTripState()
                findNavController().navigate(R.id.action_routeManager_to_mainScreenManager)
            }
        }

        binding.MapBtn.setOnClickListener {
            findNavController().navigate(R.id.action_routeManager_to_mapFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore data if available
        loadFormData()
    }

    private fun saveTrip() : Boolean {
        var result = false
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.trip.collectLatest { trip ->
                    if (trip != null) {
                        val tripInfo = formToTripInfoObject()
                        tripViewModel.addTripWithInfo(trip, tripInfo)
                        result = true //todo add conditioning
                    } else {
                        Log.e(TAG, "Error saving trip, trip object transferred from step 1 is null")
                    }
                }
            }
        }
        return result
    }

    private fun formToTripInfoObject(passedTripId: Long? = null): TripInfo {
        val title = binding.RouteTitle.text.toString().takeIf { it.isNotEmpty() } ?: sharedViewModel.tripInfo.value?.title?.takeIf { it.isNotEmpty() } ?: ""
        val points = listOf<Point>() ?: sharedViewModel.tripInfo.value?.points
        val routeDescription = binding.RouteDescription.text.toString().takeIf { it.isNotEmpty() }
            ?: sharedViewModel.tripInfo.value?.routeDescription  //todo check

        val selectedDifficulty = binding.DifficultySpinner.selectedItem as String
        val selectedArea = binding.AreaSpinner.selectedItem as String

        val areaId = -1 //TripInfoUtils.mapAreaToModel(requireContext(), selectedArea)
        val subAreaId = TripInfoUtils.mapAreaToModel(requireContext(), selectedArea)
            ?: sharedViewModel.tripInfo.value?.areaId ?: -1 //todo check
        val difficulty = TripInfoUtils.mapDifficultyToModel(requireContext(), selectedDifficulty).takeIf { it != DifficultyLevel.UNSET } //todo check
            ?: sharedViewModel.tripInfo.value?.difficulty
            ?: DifficultyLevel.UNSET

        val length = 0.0f
        val tags = listOf<String>()
        val isCircular = false
        val likes = 0
        val description = ""
        val tripId = passedTripId ?: sharedViewModel.trip.value?.id ?: 0
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
            tripId = tripId
        )
        return tripInfo
    }

    private fun verifyRouteForm(): Boolean {
        return binding.RouteTitle.toString().isNotEmpty()
    }



    private fun loadFormData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.v(TAG, "Loading route data into form")
                sharedViewModel.tripInfo.collectLatest { tripInfo ->
                    if (tripInfo != null) {
                        binding.RouteTitle.setText(sharedViewModel.tripInfo.value?.title)
                        binding.RouteDescription.setText(sharedViewModel.tripInfo.value?.routeDescription)
                        binding.AreaSpinner.setSelection(0)
                        binding.DifficultySpinner.setSelection(0)
                        Log.v(TAG, "Route data loaded: ${tripInfo.title}, ${tripInfo.id}")
                    } else {
                        Log.e(TAG, "No route data to load.")
                    }
                }
            }
        }
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