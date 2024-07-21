package il.co.erg.mykumve.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.databinding.ExploreBinding
import il.co.erg.mykumve.explore.ExploreAdapter
import il.co.erg.mykumve.ui.main.MainActivity
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private var _binding : ExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var exploreAdapter: ExploreAdapter
    private var currentUser: User? = null
    private val tripInfoViewModel: TripViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ExploreBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.msBtn.setOnClickListener{
            findNavController().navigate(R.id.action_exploreFragment_to_mainScreenManager)
        }

        binding.reportsBtn.setOnClickListener{
            findNavController().navigate(R.id.action_exploreFragment_to_UsersReports)
        }
        initializeComponent()

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exploreAdapter = ExploreAdapter(
            mutableListOf(),
            sharedViewModel,
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
                        tripInfoViewModel.fetchAllTripsInExplore()
                        tripInfoViewModel.tripsInfo.collectLatest { tripsInfo ->
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
                    // Perform search or filter logic here
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