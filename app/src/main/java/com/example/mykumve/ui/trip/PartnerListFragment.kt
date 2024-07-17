package com.example.mykumve.ui.trip

import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
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
import com.example.mykumve.R
import com.example.mykumve.databinding.FragmentPartnerListBinding
import com.example.mykumve.data.model.User
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PartnerListFragment : Fragment() {

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
        return binding.root
    }

    private fun handleCloseButton() {
        //            saveData() todo (save to db / cached the removed ones also)
        if (sharedTripViewModel.isCreatingTripMode) {
            findNavController().navigate(R.id.action_partnerListFragment_to_travelManager)
        } else {
            sharedTripViewModel.resetNewTripState()
            findNavController().navigate(R.id.action_partnerListFragment_to_mainScreenManager)
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
