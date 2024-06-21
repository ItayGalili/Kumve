package com.example.mykumve.ui.trip

import com.example.mykumve.util.Converters
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.util.Date

class TripManager : Fragment() {

    private var _binding : TravelManagerViewBinding? = null
    private val binding get() = _binding!!
    private val viewModel : TripViewModel by activityViewModels()

    private var imageUri: Uri? = null
    val pickImageLauncher : ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            binding.tripImage.setImageURI(it)
            requireActivity().contentResolver.takePersistableUriPermission(it!!, Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

        val hashedPass = EncryptionUtils.hashPassword("admin", "admin")
        val dummyUser = User("daniel", "eni", null, null, hashedPass, "admin")
        binding.doneBtn.setOnClickListener {

            val startDate: Date = Date(2024, 6, 1, 0,9,0)
            //TODO: Update difficulty level, date and picture sections
            val trip = Trip(
                title=binding.nameTrip.text.toString(),
                gatherTime = Converters().fromDate(startDate),
                gatherPlace="",
                notes=binding.description.text.toString(),
                participants= listOf(dummyUser),
                equipment=null,
                userId = dummyUser.id,
                tripInfoId=null)
            viewModel.addTrip(trip)

            findNavController().navigate(R.id.action_travelManager_to_mainScreenManager)
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