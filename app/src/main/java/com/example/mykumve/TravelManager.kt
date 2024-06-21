package com.example.mykumve

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mykumve.databinding.TravelManagerViewBinding

class TravelManager : Fragment() {

    private var _binding : TravelManagerViewBinding? = null
    private val binding get() = _binding!!

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
        binding.doneBtn.setOnClickListener {

            //TODO: Update difficulty level, date and picture sections
            val item = Item(binding.nameTrip.text.toString(),
                binding.description.text.toString(),
                "hard", "14.6.24", "1")
            ItemManager.add(item)
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