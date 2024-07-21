package il.co.erg.mykumve.ui.trip

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
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.data_classes.Point
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.databinding.RouteBinding
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.util.DifficultyLevel
import il.co.erg.mykumve.util.TripInfoUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class RouteManager : Fragment() {
    private var _binding: RouteBinding? = null
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
        _binding = RouteBinding.inflate(inflater, container, false)

        setupSpinners()

        binding.routeSaveButton.setOnClickListener {
            if (verifyRouteForm()) {
                viewLifecycleOwner.lifecycleScope.launch {
                    val saveResult = saveTrip()
                    Log.d(TAG, "Save result: $saveResult")
                }
                // Observe the operation result
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        tripViewModel.operationResult.collectLatest { result ->
                            if (result.status == Status.SUCCESS) {
                                Log.d(TAG, "Operation succeeded: ${result.message}")
                                sharedViewModel.isEditingExistingTrip = false
                                sharedViewModel.resetNewTripState()
                                findNavController().navigate(R.id.action_routeManager_to_mainScreenManager)
                            } else {
                                Log.e(TAG, "Operation failed: ${result.message}")
                            }
                        }
                    }
                }
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
        Log.d(
            TAG,
            "Creating mode: ${sharedViewModel.isCreatingTripMode}\nEditing mode: ${sharedViewModel.isEditingExistingTrip}"
        )
        sharedViewModel.isNavigatedFromTripList = false
    }

    private suspend fun saveTrip(): Boolean {
        val trip = sharedViewModel.trip.firstOrNull()?.copy(tripInfoId = "05WMl7n2srKicjVqeQnE")
        if (trip == null) {
            Log.e(TAG, "saveTrip: trip is Null")
            return false
        }
        var result = false
        val job = viewLifecycleOwner.lifecycleScope.launch {
            if (!sharedViewModel.isEditingExistingTrip) { // creating new trip
                tripViewModel.addTrip(trip)
            } else {
                tripViewModel.updateTrip(trip)
            }
            result = true
        }
        job.join() // Ensure the coroutine completes before returning
        return result
    }

    private fun formToTripInfoObject(passedTripId: String? = null): TripInfo {
        val title = binding.RouteTitle.text.toString().takeIf { it.isNotEmpty() }
            ?: sharedViewModel.tripInfo.value?.title?.takeIf { it.isNotEmpty() } ?: ""
        val points = listOf<Point>() ?: sharedViewModel.tripInfo.value?.points
        val routeDescription = binding.RouteDescription.text.toString().takeIf { it.isNotEmpty() }
            ?: sharedViewModel.tripInfo.value?.routeDescription

        val selectedDifficulty = binding.DifficultySpinner.selectedItem as String
        val selectedArea = binding.AreaSpinner.selectedItem as String

        val areaId = -1 //TripInfoUtils.mapAreaToModel(requireContext(), selectedArea)
        val subAreaId = TripInfoUtils.mapAreaToModel(requireContext(), selectedArea)
            ?: sharedViewModel.tripInfo.value?.areaId ?: -1
        val difficulty = TripInfoUtils.mapDifficultyToModel(requireContext(), selectedDifficulty)
            .takeIf { it != DifficultyLevel.UNSET }
            ?: sharedViewModel.tripInfo.value?.difficulty
            ?: DifficultyLevel.UNSET

        val length = 0.0f
        val tags = listOf<String>()
        val isCircular = false
        val likes = 0
        val description = ""
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
                        binding.RouteTitle.setText(tripInfo.title)
                        binding.RouteDescription.setText(tripInfo.routeDescription)
                        setArea(tripInfo.subAreaId)
                        setDifficulty(tripInfo.difficulty)
                        Log.d(
                            TAG,
                            "Route data loaded. title: ${tripInfo.title}, id: ${tripInfo.id}"
                        )
                        Log.d(
                            TAG,
                            "Trip data: ${sharedViewModel.trip.value?.title}, id: ${sharedViewModel.trip.value?.id}"
                        )
                    } else {
                        Log.e(TAG, "No route data to load.")
                    }
                }
            }
        }
    }

    private fun setArea(subAreaId: Int? = 0) {
        binding.AreaSpinner.setSelection(
            (binding.AreaSpinner.adapter as ArrayAdapter<String>).getPosition(
                TripInfoUtils.mapAreaToString(requireContext(), subAreaId)
            )
        )
    }

    private fun setDifficulty(difficulty: DifficultyLevel?) {
        binding.DifficultySpinner.setSelection(
            (binding.DifficultySpinner.adapter as ArrayAdapter<String>).getPosition(
                TripInfoUtils.mapDifficultyToString(requireContext(), difficulty)
            ).takeIf { it != -1 }
                ?: (binding.DifficultySpinner.adapter as ArrayAdapter<String>).getPosition(
                    getString(R.string.difficulty_unset)
                )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSpinners() {
        // Load difficulty options from strings.xml
        val difficultyOptions = resources.getStringArray(R.array.difficulty_options)
        val filteredDifficultyOptions =
            difficultyOptions.filter { it != getString(R.string.difficulty_unset) }.toTypedArray()
        val difficultyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filteredDifficultyOptions
        )
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.DifficultySpinner.adapter = difficultyAdapter

        // Load area options from strings.xml
        val areaOptions = resources.getStringArray(R.array.area_options)
        val areaAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, areaOptions)
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.AreaSpinner.adapter = areaAdapter
    }
}