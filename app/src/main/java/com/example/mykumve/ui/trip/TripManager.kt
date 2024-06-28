package com.example.mykumve.ui.trip

import android.app.DatePickerDialog
import com.example.mykumve.util.Converters
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.example.mykumve.util.ShareLevel
import com.example.mykumve.util.UserManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale



class TripManager : Fragment() {

    private var _binding: TravelManagerViewBinding? = null
    private val binding get() = _binding!!
    private val tripViewModel: TripViewModel by activityViewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private var currentUser: User? = null
    private var selectedDate: String? = null  // Add this variable to store the selected date


    private var imageUri: Uri? = null
    val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.tripImage.setImageURI(it)
            requireActivity().contentResolver.takePersistableUriPermission(
                it!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imageUri = it
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        binding.mapBtn.setOnClickListener{
            findNavController().navigate(R.id.action_travelManager_to_mapFragmentManager)
        }



        //date
        binding.dateBtn.setOnClickListener {
            showCalendar(it)
        }

        //equipment list:
        binding.listBtn.setOnClickListener {
            findNavController().navigate(R.id.action_travelManager_to_equipmentFragment)
        }

        binding.doneBtn.setOnClickListener {
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

    private fun showCalendar(button: View?) {
        val c = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { dataPicker, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateString = dateFormat.format(calendar.time)
            binding.datePick.text = dateString
            selectedDate = dateString  // Store the selected date
        }
        val dtd = DatePickerDialog(requireContext(), listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        dtd.show()
    }

    private fun addTrip(button: View?, user: User, equipmentList: List<Equipment>?) {
        val title = binding.nameTrip.text.toString()
        if (title.isBlank()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert startDate to a timestamp
        val gatherTime = Converters().stringToDate(selectedDate.toString())
        val endTime =  null
        val equipments = equipmentList?.toMutableList()  // Changed line to convert List<Equipment> to MutableList<Equipment>
        val image = null
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
        findNavController().navigate(R.id.action_travelManager_to_mainScreenManager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}