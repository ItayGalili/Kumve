package com.example.mykumve.ui.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mykumve.R

/**
 * Fragment displaying a list of trips.
 * Manages the trip list UI and interactions.
 *
 * TODO: Implement RecyclerView to display list of trips.
 */
class TripListFragment : Fragment() {

    private lateinit var travelManager: TravelManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.inflate(R.layout.trip_list, container, false)
        travelManager = TravelManager(requireContext())

        // TODO: Initialize RecyclerView and load trips.

        return view
    }
}
