package il.co.erg.mykumve.ui.explore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.databinding.ExploreBinding
import il.co.erg.mykumve.ui.main.MainActivity
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {
    val TAG = ExploreFragment::class.java.simpleName
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private var _binding : ExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var exploreAdapter: ExploreAdapter
    private var currentUser: User? = null
    private val tripViewModel: TripViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ExploreBinding.inflate(inflater, container, false)
        val view = binding.root
        sharedViewModel.isNavigatedFromExplore = true
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.exploreFragment)
        }

        binding.msBtn.setOnClickListener{
            findNavController().navigate(R.id.action_exploreFragment_to_mainScreenManager)
        }

        binding.reportsBtn.setOnClickListener{
            findNavController().navigate(R.id.action_exploreFragment_to_UsersReports)
        }
        initializeComponent()

        Log.d(
            TAG,
            "Creating mode: ${sharedViewModel.isCreatingTripMode}\n" +
                    "Editing mode: ${sharedViewModel.isEditingExistingTrip}\n" +
                    "Navigated from Explore: ${sharedViewModel.isNavigatedFromExplore}\n" +
                    "Navigated from Trip List: ${sharedViewModel.isNavigatedFromTripList}\n" +
                    "is Exactly one Trip checked: ${sharedViewModel.isExactlyOneTripIsChecked}\n"
        )
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exploreAdapter = ExploreAdapter(
            mutableListOf(),
            sharedViewModel,
            tripViewModel,
            requireContext(),
            lifecycleOwner = viewLifecycleOwner,
        )
        binding.exploreRecyclerView.adapter = exploreAdapter
        binding.exploreRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        if (UserManager.isLoggedIn()) {
            currentUser = UserManager.getUser()
            currentUser?.let { user ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        tripViewModel.fetchAllTripsInfo()
                        tripViewModel.tripsInfo.collectLatest { tripsInfo ->
                            Log.d(TAG,"${tripsInfo.size} trips info found")
                            exploreAdapter.updateTripList(tripsInfo)
                            exploreAdapter.notifyDataSetChanged()
                            if (MainActivity.DEBUG_MODE) {
                                //logTripInfo(tripsInfo) Waiting for Daniel's respond
                            }
                        }
                    }
                }
            }
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_exploreFragment_to_loginManager)
        }
        binding.searchView.clearFocus()
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission
                query?.let {
                    Toast.makeText(context,
                        "Kumve found ${exploreAdapter.itemCount} trips that matches your search",
                        Toast.LENGTH_SHORT).show()
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    exploreAdapter.filterByQuery(newText)
                }
                return true
            }
        })
    }

    private fun initializeComponent() {
        sharedViewModel.isEditingExistingTrip = false
        sharedViewModel.isCreatingTripMode = false
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}