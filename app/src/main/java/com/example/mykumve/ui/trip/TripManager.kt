package com.example.mykumve.ui.trip

import com.example.mykumve.util.Converters
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mykumve.R
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.TravelManagerViewBinding
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.EncryptionUtils
import com.example.mykumve.util.UserManager
import java.util.Date

class TripManager : Fragment() {

    private var _binding: TravelManagerViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TripViewModel by activityViewModels()
    private var currentUser: User? = null

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
            // Use the user object as needed
            Toast.makeText(
                requireContext(),
                getString(R.string.welcome_user, currentUser?.firstName),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            // You can navigate to the login screen or take appropriate action
        }

        binding.doneBtn.setOnClickListener {
            // Check if currentUser is not null
            currentUser?.let { user ->
                val startDate: Date = Date(2024, 6, 1, 0, 9, 0)
                // Convert startDate to a timestamp
                val gatherTime = Converters().fromDate(startDate)

                // Create a new Trip object with the provided details
                val trip = Trip(
                    title = binding.nameTrip.text.toString(),
                    gatherTime = gatherTime,
                    gatherPlace = "",
                    notes = binding.description.text.toString(),
                    participants = listOf(user),
                    equipment = null,
                    userId = user.id,
                    tripInfoId = null
                )

                // Add the trip to the viewModel
                viewModel.addTrip(trip)

                // Navigate to the main screen
                findNavController().navigate(R.id.action_travelManager_to_mainScreenManager)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}