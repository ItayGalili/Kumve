package com.example.mykumve.ui.trip

/**
 * Fragment for creating a new trip.
 * Manages the trip creation UI and interactions.
 *
 * TODO: Implement form fields for trip creation and validation.
 */
class TripCreationFragment : Fragment() {

    private lateinit var travelManager: TravelManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.trip_creation, container, false)
        travelManager = TravelManager(requireContext())

        // TODO: Implement form fields and handle trip creation.

        return view
    }
}
