package com.example.mykumve.ui.trip

import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykumve.R
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.databinding.FragmentPartnerListBinding
import com.example.mykumve.data.model.User
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.TripInvitationStatus
import com.example.mykumve.util.UserManager


class PartnerListFragment : Fragment() {

    private lateinit var binding: FragmentPartnerListBinding
    private lateinit var partnerListAdapter: PartnerListAdapter
    private val sharedTripViewModel: SharedTripViewModel by activityViewModels()
    private val tripViewModel: TripViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private var selectedUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPartnerListBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeTripInvitations()
        logPossiblePartner()

        binding.addPartner.setOnClickListener {
            invitePartnerToTrip(it)
        }

        binding.closePartnerBtn.setOnClickListener{
//            saveData()
            if (sharedTripViewModel.isCreatingTripMode) {
                findNavController().navigate(R.id.action_partnerListFragment_to_travelManager)
            } else {
                sharedTripViewModel.resetNewTripState()
                findNavController().navigate(R.id.action_partnerListFragment_to_mainScreenManager)
            }
        }
        return binding.root
    }

    private fun invitePartnerToTrip(button: View?) {
        val phoneNumber = binding.phoneNumberToInvite.text.toString() // Corrected to get the text
        Log.d("InvitePartner", "Phone number to invite: $phoneNumber")

        userViewModel.getUserByPhone(phoneNumber)?.observe(viewLifecycleOwner, Observer { user ->
            if (user != null) {
                Log.d(
                    "InvitePartner",
                    "User found: ${user.firstName} ${user.surname} with ID ${user.id}"
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
                    } else {
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
                Log.d("PartnerListFragment", "Users fetched: $users")
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
                    partnerListAdapter.submitList(partialTrip.invitations.toMutableList())
                } else {
                    // For existing trip (selectedExistingTrip)
                    tripViewModel.getTripInvitationsByTripId(it.id)?.observe(viewLifecycleOwner, Observer { invitations ->
                        partnerListAdapter.submitList(invitations.toMutableList())
                    })
                }
            } ?: run {
                partnerListAdapter.submitList(mutableListOf())
            }
        })
    }


    private fun setupRecyclerView() {
        partnerListAdapter = PartnerListAdapter(userViewModel, viewLifecycleOwner)
        binding.invitationList.adapter = partnerListAdapter
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
                val position = viewHolder.adapterPosition
                val invitation = partnerListAdapter.getInvitationAtPosition(position)
                partnerListAdapter.removeInvitationAtPosition(position)
                if (invitation.status == TripInvitationStatus.PENDING) {
                    tripViewModel.deleteTripInvitation(invitation)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.invitationList)
    }

}
