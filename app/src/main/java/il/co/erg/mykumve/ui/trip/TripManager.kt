package il.co.erg.mykumve.ui.trip

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.data_classes.Equipment
import il.co.erg.mykumve.data.db.model.Trip
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.databinding.TravelManagerViewBinding
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripWithInfo
import il.co.erg.mykumve.util.ImagePickerUtil
import il.co.erg.mykumve.util.ShareLevel
import il.co.erg.mykumve.util.UserManager
import il.co.erg.mykumve.util.Utility.timestampToString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TripManager : Fragment() {
    val TAG = TripManager::class.java.simpleName
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
        Log.d(TAG, "On view created")
        // Logic to determine if it's a new trip creation
        loadFormData()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            sharedViewModel.isEditingExistingTrip = false
            findNavController().navigate(R.id.action_travelManager_to_mainScreenManager)
        }
        Log.d(TAG, "Creating mode: ${sharedViewModel.isCreatingTripMode}\nEditing mode: ${sharedViewModel.isEditingExistingTrip}")
        sharedViewModel.isNavigatedFromTripList = false
    }

    private fun loadFormData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                Log.v(TAG, "Loading trip data into form")
                sharedViewModel.trip.collectLatest { trip ->
                    if (trip != null) {
                        binding.tripImage.setImageURI(trip.image?.toUri())
                        binding.nameTrip.setText(trip.title)
                        binding.description.setText(trip.description.toString())
                        binding.dateStartPick.text = timestampToString(trip.gatherTime)
                        binding.dateEndPick.text = timestampToString(trip.endDate)
                        Log.d(TAG, "Trip data loaded. title: ${trip.title}, id: ${trip.id}")
                    } else {
                        Log.e(TAG, "No trip data to load.")
                    }
                sharedViewModel.tripInfo.collectLatest { tripInfo ->
                    Log.d(TAG, "loadFromData, trip info title: ${tripInfo?.title}, trip info id: ${tripInfo?.id}")
                }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TravelManagerViewBinding.inflate(inflater, container, false)
        Log.d(TAG, "On create view")

        if (UserManager.isLoggedIn()) {
            currentUser = UserManager.getUser()
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
        }

        imagePickerUtil = ImagePickerUtil(this,
            onImagePicked = { uri ->
                binding.tripImage.setImageURI(uri)
            },
            onImageUploadResult = { success, downloadUrl ->
                if (success && downloadUrl != null) {
                    // Handle successful upload if needed
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.dateStartBtn.setOnClickListener {
            showDateTimePicker(true)
        }

        binding.dateEndBtn.setOnClickListener {
            showDateTimePicker(false)
        }
        if(sharedViewModel.isCreatingTripMode){
            binding.tripSaveBtn.isVisible=false
            binding.NextBtn.isVisible=true
        }
        else{
            binding.tripSaveBtn.isVisible=true
            binding.NextBtn.isVisible=false
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
            currentUser?.let { user ->
                if (verifyTripForm()) {
                    cacheTrip()
                    viewLifecycleOwner.lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            sharedViewModel.trip.collectLatest { trip ->
                                if (trip != null) {
                                    findNavController().navigate(R.id.action_travelManager_to_routeManager)
                                    return@collectLatest
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            }
        }

        binding.tripImage.setOnClickListener {
            imagePickerUtil.pickImage()
        }
        return binding.root
    }

    private fun verifyTripForm(): Boolean {
        if (binding.nameTrip.text.toString().isBlank()) {
            Toast.makeText(requireContext(), R.string.title_is_required, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun cacheTrip() {
        currentUser?.let { user ->
            Log.d(
                TAG,
                "Caching trip." + if (sharedViewModel.isCreatingTripMode)
                    " Creating trip mode" else " Selecting existing trip"
            )

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    Log.v(TAG, "Loading trip data into form")
                    sharedViewModel.trip.collectLatest { trip ->
                        var tempTrip: Trip? = null
                        var tempTripInfo: TripInfo? = null
                        if (trip != null) {
                            tempTrip = formToTripObject(user, tripFromSharedViewModel = trip)
                            tempTripInfo = sharedViewModel.tripInfo.value // Assuming tripInfo is already set
                        } else {
                            tempTrip = formToTripObject(user )
                        }
                        Log.d(TAG, "Caching trip id: ${tempTrip.id} and tripInfo: ${tempTripInfo?.id}")
                        if (sharedViewModel.isCreatingTripMode) {
                            sharedViewModel.setPartialTrip(tempTrip)
                            tempTripInfo?.let { sharedViewModel.setPartialTripInfo(it) }
                        } else {
                            val existingTripWithInfo = TripWithInfo(tempTrip, tempTripInfo)
                            sharedViewModel.selectExistingTripWithInfo(existingTripWithInfo)
                        }
                    }
                }
            }



        }
    }

    private fun showDateTimePicker(isStartDate: Boolean, eventLength: Int = 4 * 60 * 60 * 1000) {
        val c = Calendar.getInstance()

        // Set date picker to allow only future dates
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)

                if (isStartDate) {
                    if (calendar.timeInMillis < System.currentTimeMillis()) {
                        Toast.makeText(
                            requireContext(),
                            "Start date cannot be before current date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    startDate = calendar.timeInMillis
                    binding.dateStartPick.text = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(calendar.time)
                } else {
                    if (startDate == null) {
                        Toast.makeText(
                            requireContext(),
                            "Please select a start date first",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    if (calendar.timeInMillis < startDate!!) {
                        Toast.makeText(
                            requireContext(),
                            "End date cannot be before start date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    val diff = calendar.timeInMillis - startDate!!
                    if (diff < 3600000) {
                        Toast.makeText(
                            requireContext(),
                            "End date must be at least 1 hour after start date",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@OnTimeSetListener
                    }
                    endDate = calendar.timeInMillis
                    binding.dateEndPick.text = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    ).format(calendar.time)
                }
            }
            TimePickerDialog(
                requireContext(),
                timeListener,
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
            ).show()
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateListener,
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        )

        // Limit date picker to future dates only
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()

        if (!isStartDate && startDate != null) {
            // Limit end date picker to dates after the start date
            datePickerDialog.datePicker.minDate = startDate!!
        }

        datePickerDialog.show()
    }

    private fun formToTripObject(
        user: User,
        tripFromSharedViewModel: Trip? = null,

        ): Trip {
        val id = sharedViewModel.tripInfo.value?.id ?: ""
        val title = binding.nameTrip.text.toString()
        val description = binding.description.text.toString()
        val gatherTime = startDate ?: sharedViewModel.trip.value?.gatherTime
        val endTime = endDate ?: sharedViewModel.trip.value?.endDate
        val equipments = tripFromSharedViewModel?.equipment?.toMutableList()

        val participantsIds = mutableListOf(user.id)

        val invitationsIds = tripFromSharedViewModel?.invitationIds?.takeIf { it.isNotEmpty() }?.toMutableList()
            ?: mutableListOf()

        val photo = imagePickerUtil.downloadUrl.toString().takeIf { it != "null" }
            ?: tripFromSharedViewModel?.image
        val notes = null

        // Create a new Trip object with the provided details
        val trip = Trip(
            _id = id,
            title = title,
            gatherTime = gatherTime,
            endDate = endTime,
            description = description,
            notes = notes,
            participantIds = participantsIds,
            invitationIds = invitationsIds,
            equipment = equipments,
            userId = user.id,
            image = photo,
            tripInfoId = null,
            shareLevel = ShareLevel.PUBLIC,
        )
        return trip
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate called")
    }

    override fun onStart() {
        super.onStart()
        Log.v(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.v(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.v(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy called")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
