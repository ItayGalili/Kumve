package com.example.mykumve.ui.trip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.TravelManagerViewBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.Converters
import com.example.mykumve.util.NavigationArgs
import com.example.mykumve.util.ShareLevel
import com.example.mykumve.util.UserManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripManager : Fragment() {

    private var _binding: TravelManagerViewBinding? = null
    private val binding get() = _binding!!
    private val tripViewModel: TripViewModel by activityViewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private var currentUser: User? = null

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var imageUri: Uri? = null

    val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.tripImage.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.tripImage.setImageURI(it)
            requireActivity().contentResolver.takePersistableUriPermission(
                it!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imageUri = it
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Logic to determine if it's a new trip creation
        val isCreatingNewTrip = arguments?.getBoolean(NavigationArgs.IS_CREATING_NEW_TRIP.key, false) ?: false

        if (isCreatingNewTrip) {
            sharedViewModel.resetNewTripState()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TravelManagerViewBinding.inflate(inflater, container, false)

        if (UserManager.isLoggedIn()) {
            currentUser = UserManager.getUser()

        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            // You can navigate to the login screen or take appropriate action
        }

        //date
        binding.dateStartBtn.setOnClickListener {
            showDateTimePicker(true)
        }

        binding.dateEndBtn.setOnClickListener {
            showDateTimePicker(false)
        }

        //equipment list:
        binding.listBtn.setOnClickListener {
            findNavController().navigate(R.id.action_travelManager_to_equipmentFragment)
        }

        //Partner list
        binding.PartnersBtn.setOnClickListener {
            findNavController().navigate(R.id.action_travelManager_to_partnerListFragment)
        }

        binding.NextBtn.setOnClickListener {
            // Check if currentUser is not null
            currentUser?.let { user ->
                sharedViewModel.equipmentList.observe(viewLifecycleOwner, Observer { equipmentList ->
                    addTrip(it, user, equipmentList)
                })
            } ?: run {
                // Handle the case where the user is not logged in or currentUser is null
                Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            }

        }

        binding.tripImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))

        }
        return binding.root
    }

    private fun showDateTimePicker(isStartDate: Boolean) {
        val c = Calendar.getInstance()
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)

                if (isStartDate) {
                    if (calendar.before(Calendar.getInstance())) {
                        Toast.makeText(requireContext(), "Start date cannot be before current date", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    startDate = calendar
                    binding.StartView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                } else {
                    if (startDate == null) {
                        Toast.makeText(requireContext(), "Please select a start date first", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    if (calendar.before(startDate)) {
                        Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    val diff = calendar.timeInMillis - startDate!!.timeInMillis
                    if (diff < 3600000) {
                        Toast.makeText(requireContext(), "End date must be at least 1 hour after start date", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    endDate = calendar
                    binding.EndView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                }
            }
            TimePickerDialog(requireContext(), timeListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
        DatePickerDialog(requireContext(), dateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    // todo - move to next fragment (page) of add info to trip (trip info)
    private fun addTrip(button: View?, user: User, equipmentList: List<Equipment>?) {
        val title = binding.nameTrip.text.toString()
        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert startDate to a timestamp
        val gatherTime = startDate?.timeInMillis
        val endTime = endDate?.timeInMillis
        val equipments = equipmentList?.toMutableList()  // Changed line to convert List<Equipment> to MutableList<Equipment>
        val image = imageUri?.toString()

        // Create a new Trip object with the provided details
        val trip = Trip(
            title = title,
            gatherTime = gatherTime,
            endDate = endTime,
            notes = mutableListOf(binding.description.text.toString()),
            participants = mutableListOf(user),
            equipment = equipments,
            userId = user.id,
            image = image,
            tripInfoId = null,
            shareLevel = ShareLevel.PUBLIC,
        )

        // Add the trip to the viewModel
        tripViewModel.addTrip(trip)

        // Navigate to the main screen
        findNavController().navigate(R.id.action_travelManager_to_routeManager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
