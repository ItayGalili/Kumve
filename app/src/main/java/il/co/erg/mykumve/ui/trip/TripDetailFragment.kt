package il.co.erg.mykumve.ui.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Fragment displaying trip details.
 * Manages the trip detail UI and interactions.
 *
 * TODO: Implement methods to display trip details.
 */
class TripDetailFragment : Fragment() {

    private lateinit var tripManager: TripManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val view = inflater.inflate(R.layout.trip_detail, container, false)
        tripManager = TripManager()

        // TODO: Load and display trip details.

        return view
    }
}
