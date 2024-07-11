package com.example.mykumve.ui.trip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.TravelManagerViewBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.ImagePickerUtil
import com.example.mykumve.util.ShareLevel
import com.example.mykumve.util.UserManager
import com.example.mykumve.util.Utility.timestampToString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripManager : Fragment() {

    private var _binding: TravelManagerViewBinding? = null
    private val binding get() = _binding!!
    private val tripViewModel: TripViewModel by activityViewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private var currentUser: User? = null

    private var startDate: Long? = null
    private var endDate: Long? = null
    private lateinit var imagePickerUtil: ImagePickerUtil


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Logic to determine if it's a new trip creation
        val isCreatingNewTrip = true // todo - verify; is it always true? arguments?.getBoolean(NavigationArgs.IS_CREATING_NEW_TRIP.key, false) ?: false

        if (isCreatingNewTrip && sharedViewModel.trip.value == null) {
            // There is no cached trip and in creating trip fragment then reset state
            sharedViewModel.resetNewTripState()
        }

        // Restore data if available
        sharedViewModel.trip.value?.let { trip ->
            binding.tripImage.setImageURI(trip.image?.toUri())
            binding.nameTrip.setText(trip.title)
            binding.description.setText(trip.description.toString())
            binding.dateStartPick.text = timestampToString(trip.gatherTime)
            binding.dateEndPick.text = timestampToString(trip.endDate)
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

        }

        imagePickerUtil = ImagePickerUtil(this) { uri ->
            binding.tripImage.setImageURI(uri)
        }

        binding.dateStartBtn.setOnClickListener {
            showDateTimePicker(true)
        }

        binding.dateEndBtn.setOnClickListener {
            showDateTimePicker(false)
        }

        //equipment list:
        binding.equipmentListBtn.setOnClickListener {
            cacheTrip()
            findNavController().navigate(R.id.action_travelManager_to_equipmentFragment)
        }

        //Partner list
        binding.PartnersBtn.setOnClickListener {
            cacheTrip()
            findNavController().navigate(R.id.action_travelManager_to_partnerListFragment)
        }

        binding.NextBtn.setOnClickListener {
            // Check if currentUser is not null
            currentUser?.let { user ->
                sharedViewModel.equipmentList.observe(viewLifecycleOwner, Observer { equipmentList ->
                    cacheTrip()
                    findNavController().navigate(R.id.action_travelManager_to_routeManager)                })
            } ?: run {
                // Handle the case where the user is not logged in or currentUser is null
                Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            }

        }

        binding.tripImage.setOnClickListener {
            imagePickerUtil.pickImage()
        }
        return binding.root
    }

    private fun cacheTrip() {
        currentUser?.let { user ->
            val tempTrip = formToTripObject(user)
            sharedViewModel.setPartialTrip(tempTrip)
        }
    }

    private fun showDateTimePicker(isStartDate: Boolean) {
        val c = Calendar.getInstance()
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)

                if (isStartDate) {
                    if (calendar.timeInMillis < System.currentTimeMillis()) {
                        Toast.makeText(requireContext(), "Start date cannot be before current date", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    startDate = calendar.timeInMillis
                    binding.StartView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                } else {
                    if (startDate == null) {
                        Toast.makeText(requireContext(), "Please select a start date first", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    if (calendar.timeInMillis < startDate!!) {
                        Toast.makeText(requireContext(), "End date cannot be before start date", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    val diff = calendar.timeInMillis - startDate!!
                    if (diff < 3600000) {
                        Toast.makeText(requireContext(), "End date must be at least 1 hour after start date", Toast.LENGTH_SHORT).show()
                        return@OnTimeSetListener
                    }
                    endDate = calendar.timeInMillis
                    binding.EndView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
                }
            }
            TimePickerDialog(requireContext(), timeListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }
        DatePickerDialog(requireContext(), dateListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    // todo - move to next fragment (page) of add info to trip (trip info)
    private fun addTrip(button: View?, user: User, equipmentList: List<Equipment>?) {
        if (binding.nameTrip.text.toString().isBlank()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
            return
        }

        val trip = formToTripObject(user, equipmentList)

        // Add the trip to the viewModel
        tripViewModel.addTrip(trip)

    }

    private fun formToTripObject(
        user: User,
        equipmentList: List<Equipment>? = null,
        participantList: List<User>? = null,
        invitationList: List<TripInvitation>? = null,

    ): Trip {
        val title = binding.nameTrip.text.toString()
        val description = binding.description.text.toString()
        val gatherTime = startDate
        val endTime = endDate
        val equipments = equipmentList?.takeIf { it.isNotEmpty() }?.toMutableList() ?:
        sharedViewModel.trip.value?.equipment?.toMutableList()

        val participants = participantList?.takeIf { it.isNotEmpty() }?.toMutableList() ?:
        mutableListOf(user)

        val invitations = invitationList?.takeIf { it.isNotEmpty() }?.toMutableList() ?:
        sharedViewModel.trip.value?.invitations?.takeIf { it.isNotEmpty() }?.toMutableList() ?:
        mutableListOf()

        val temp = sharedViewModel.trip.value?.participants
        val photo = imagePickerUtil.getImageUri()?.toString()
        val notes = null

        // Create a new Trip object with the provided details
        val trip = Trip(
            title = title,
            gatherTime = gatherTime,
            endDate = endTime,
            description = description,
            notes = notes,
            participants = participants,
            invitations = invitations,
            equipment = equipments,
            userId = user.id,
            image = photo,
            tripInfoId = null,
            shareLevel = ShareLevel.PUBLIC,
        )
        return trip
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
