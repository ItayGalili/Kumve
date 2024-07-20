package il.co.erg.mykumve.ui.trip

import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import il.co.erg.mykumve.R
import il.co.erg.mykumve.databinding.FragmentPartnerListBinding
import il.co.erg.mykumve.data.model.User
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PartnerListFragment : Fragment() {

    val TAG = PartnerListFragment::class.java.simpleName
    private lateinit var binding: FragmentPartnerListBinding
    private lateinit var partnerListAdapter: PartnerListAdapter
    private lateinit var currentUser: User
    private val sharedTripViewModel: SharedTripViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPartnerListBinding.inflate(inflater, container, false)

        if (UserManager.isLoggedIn()) {
            UserManager.getUser()?.let { user ->
                currentUser = user
            }
        }
        setupRecyclerView()
        observeTripPartners()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleCloseButton()
        }
        binding.showInvitationBtn.setOnClickListener{
            findNavController().navigate(R.id.action_partnerListFragment_to_invitationListFragment)
        }
        Log.d(TAG, "Creating mode: ${sharedTripViewModel.isCreatingTripMode}\nEditing mode: ${sharedTripViewModel.isEditingExistingTrip}")
        return binding.root
    }

    private fun handleCloseButton() {
        if (sharedTripViewModel.isNavigatedFromTripList) {
            sharedTripViewModel.resetNewTripState()
            findNavController().navigate(R.id.action_partnerListFragment_to_mainScreenManager)
        } else {
            findNavController().navigate(R.id.action_partnerListFragment_to_travelManager)
        }
    }

    private fun observeTripPartners() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedTripViewModel.trip.collectLatest { trip ->
                    if (trip != null) {
                        val participants = trip.participants?.toMutableList() ?: mutableListOf()
                        partnerListAdapter.submitList(participants)
                    }
                }
            }
        }
    }


    private fun setupRecyclerView() {
        partnerListAdapter = PartnerListAdapter(userViewModel, viewLifecycleOwner)
        binding.participantsList.adapter = partnerListAdapter
        binding.participantsList.layoutManager = LinearLayoutManager(requireContext())

        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val position = viewHolder.adapterPosition
//                val invitation = partnerListAdapter.getInvitationAtPosition(position)
//                partnerListAdapter.removeInvitationAtPosition(position)
//                if (invitation.status == TripInvitationStatus.PENDING) {
//                    tripViewModel.deleteTripInvitation(invitation)
//                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.participantsList)
    }

}
