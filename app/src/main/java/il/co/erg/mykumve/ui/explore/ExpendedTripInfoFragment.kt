package il.co.erg.mykumve.ui.explore

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import il.co.erg.mykumve.R
import il.co.erg.mykumve.databinding.TripInfoBinding
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ExpendedTripInfoFragment: Fragment() {
    val TAG = ExpendedTripInfoFragment::class.java.simpleName
    private var _binding: TripInfoBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TripInfoBinding.inflate(inflater, container, false)
        binding.addTripInfoCardToMyTrips.setOnClickListener {
            it.findNavController().navigate(R.id.action_expendedTripInfoFragment_to_travelManager)
        }
        binding.goBackToExplore.setOnClickListener {
            it.findNavController().navigate(R.id.action_expendedTripInfoFragment_to_exploreFragment)
        }

        initializeComponent()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore data if available
        Log.d(
            TAG,
            "Creating mode: ${sharedViewModel.isCreatingTripMode}\n" +
                    "Editing mode: ${sharedViewModel.isEditingExistingTrip}\n" +
                    "Navigated from Explore: ${sharedViewModel.isNavigatedFromExplore}\n" +
                    "Navigated from Trip List: ${sharedViewModel.isNavigatedFromTripList}\n" +
                    "is Exactly one Trip checked: ${sharedViewModel.isExactlyOneTripIsChecked}\n"
        )
        loadFormData()
        Log.d(
            TAG,
            "Creating mode: ${sharedViewModel.isCreatingTripMode}\n" +
                    "Editing mode: ${sharedViewModel.isEditingExistingTrip}\n" +
                    "Navigated from Explore: ${sharedViewModel.isNavigatedFromExplore}\n" +
                    "Navigated from Trip List: ${sharedViewModel.isNavigatedFromTripList}\n" +
                    "is Exactly one Trip checked: ${sharedViewModel.isExactlyOneTripIsChecked}\n"
        )
    }
    private fun loadFormData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val tripInfo = sharedViewModel.tripInfo.firstOrNull()
                if (tripInfo != null) {
                    binding.tripDescription.text = tripInfo.description
                    binding.tripDifficulty.text = context?.getString(tripInfo.difficulty.prettyString())
                    binding.tripName.setText(tripInfo.title)
                    if (tripInfo.length?.isNaN() == false) {
                        binding.tripLength.text = buildString {
                            append((tripInfo.length.toString()))
                            append(" km")
                        }
                    } else {
                        binding.tripLength.text = "Length is not specified"
                    }
                    sharedViewModel.tripInfo.collectLatest { tripInfo ->
                        Log.d(
                            TAG,
                            "loadFromData, trip info title: ${tripInfo?.title}, trip info id: ${tripInfo?.id}"
                        )
                    }
                } else{
                        Log.e(TAG, "No route data to load.")
                }
            }
        }

    }
    private fun initializeComponent() {
        sharedViewModel.isEditingExistingTrip = false
        sharedViewModel.isCreatingTripMode = false
    }
}
