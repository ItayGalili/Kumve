package il.co.erg.mykumve.explore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Handles network operations, initially for local data and later for Firebase.
 * Manages data synchronization and network requests.
 *
 * TODO: Implement methods for handling network requests and data synchronization.
 */
class ExploreFragment : Fragment() {

    val TAG = ExploreFragment::class.java.simpleName
    private val tripViewModel: TripViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripViewModel.fetchAllTripsInfo()
                tripViewModel.tripsInfo.collectLatest { tripsInfo ->
                    Log.d(TAG, "Found ${tripsInfo.size} Trips Infos")
                }
            }
        }

        return view
    }


    // TODO: Implement methods to handle network operations.

}
