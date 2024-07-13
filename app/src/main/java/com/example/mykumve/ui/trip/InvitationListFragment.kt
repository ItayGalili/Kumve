package com.example.mykumve.ui.trip

import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykumve.R
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.FragmentInvitationListBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.TripInvitationStatus
import com.example.mykumve.util.UserManager
import com.example.mykumve.util.UserUtils


class InvitationListFragment : Fragment() {

    private lateinit var binding: FragmentInvitationListBinding
    private lateinit var invitationListAdapter: InvitationListAdapter
    private lateinit var currentUser: User
    private val sharedTripViewModel: SharedTripViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInvitationListBinding.inflate(inflater, container, false)

        if (UserManager.isLoggedIn()) {
            UserManager.getUser()?.let { user ->
                currentUser = user
            }
        }
        setupRecyclerView()
        observeTripInvitations()
        logPossiblePartner()

        binding.addPartner.setOnClickListener {
            invitePartnerToTrip(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleCloseButton()
        }
        binding.closePartnerBtn.setOnClickListener{
            handleCloseButton()
        }
        return binding.root
    }

    private fun handleCloseButton() {
        //            saveData() todo (save to db / cached the removed ones also)
        findNavController().navigate(R.id.action_invitationListFragment_to_partnerListFragment)
    }

    private fun invitePartnerToTrip(button: View?) {
        val phoneNumber = binding.phoneNumberToInvite.text.toString() // Corrected to get the text
        if (phoneNumber.isEmpty())
            return
        Log.d("InvitePartner", "Phone number to invite: $phoneNumber")

        userViewModel.getUserByPhone(phoneNumber)?.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                Log.d(
                    "InvitePartner",
                    "User found: ${UserUtils.getFullName(user)} with ID ${user.id}"
                )
                val currentTrip = sharedTripViewModel.trip.value
                if (currentTrip != null) {
                    if (sharedTripViewModel.isCreatingTripMode) {
                        val newInvitation = TripInvitation(
                            tripId = currentTrip.id,
                            userId = user.id,
                            status = TripInvitationStatus.UNSENT
                        )
                        sharedTripViewModel.addInvitation(newInvitation)
//                        partnerListAdapter.notifyDataSetChanged() // Update RecyclerView
                        Toast.makeText(requireContext(), "Invitation added", Toast.LENGTH_SHORT).show()
                    } else { // todo check?
                        tripViewModel.sendTripInvitation(
                            tripId = currentTrip.id,
                            userId = user.id
                        ) { result ->
                            if (result) {
                                Toast.makeText(requireContext(), "Sent", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to send",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "No current trip found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Log.d("InvitePartner", "User not found for phone number: $phoneNumber")
                Toast.makeText(
                    requireContext(),
                    "Can't find user with this phone number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun logPossiblePartner() {
        userViewModel.getAllUsers()?.observe(viewLifecycleOwner, Observer { users ->
            if (users != null) {
                val currentUserId = UserManager.getUser()?.id
                val filteredUsers = users.filter { it.id != currentUserId }
                Log.d(
                    "PartnerListFragment",
                    filteredUsers.map { "${it.firstName}: ${it.phone}" }.toString()
                )
            } else {
                Log.d("PartnerListFragment", "No users found.")
            }
        })
    }


    private fun observeTripInvitations() {
        sharedTripViewModel.trip.observe(viewLifecycleOwner, Observer { trip ->
            trip?.let {
                if (sharedTripViewModel.isCreatingTripMode) {
                    // For temporary trip (partialTrip)
                    val partialTrip = it
                    invitationListAdapter.submitList(partialTrip.invitations.toMutableList())
                } else {
                    // For existing trip (selectedExistingTrip)
                    tripViewModel.getTripInvitationsByTripId(it.id)?.observe(viewLifecycleOwner, Observer { invitations ->
                        invitationListAdapter.submitList(invitations.toMutableList())
                    })
                }
            } ?: run {
                invitationListAdapter.submitList(mutableListOf())
            }
        })
    }


    private fun setupRecyclerView() {
        invitationListAdapter = InvitationListAdapter(userViewModel, viewLifecycleOwner)
        binding.invitationList.adapter = invitationListAdapter
        binding.invitationList.layoutManager = LinearLayoutManager(requireContext())

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
//                val invitation = invitationListAdapter.getInvitationAtPosition(position)
//                invitationListAdapter.removeInvitationAtPosition(position)
//                if (invitation.status == TripInvitationStatus.PENDING) {
//                    tripViewModel.deleteTripInvitation(invitation)
//                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.invitationList)
    }

}
