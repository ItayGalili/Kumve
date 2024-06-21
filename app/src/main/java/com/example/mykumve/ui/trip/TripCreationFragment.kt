package com.example.mykumve.ui.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Fragment for creating a new trip.
 * Manages the trip creation UI and interactions.
 *
 * TODO: Implement form fields for trip creation and validation.
 */
class TripCreationFragment : Fragment() {

    private lateinit var tripManager: TripManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.inflate(R.layout.trip_creation, container, false)
        tripManager = TripManager()

        // TODO: Implement form fields and handle trip creation.

        return view
    }
}
