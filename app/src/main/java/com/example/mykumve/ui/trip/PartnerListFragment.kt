package com.example.mykumve.ui.trip
import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykumve.databinding.FragmentPartnerListBinding
import com.example.mykumve.data.model.User
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.TripInvitationStatus
import com.example.mykumve.util.UserManager


class PartnerListFragment : Fragment() {

    private lateinit var binding: FragmentPartnerListBinding
    private lateinit var adapter: PartnerListAdapter
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
        populatePossiblePartnerDropdown()

        return binding.root
    }

    private fun populatePossiblePartnerDropdown() {
        userViewModel.getAllUsers()?.observe(viewLifecycleOwner, Observer { users ->
            if (users != null) {
                Log.d("PartnerListFragment", "Users fetched: $users")
                val currentUserId = UserManager.getUser()?.id
                val filteredUsers = users.filter { it.id != currentUserId }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    filteredUsers.map { "${it.firstName} ${it.surname}" } // todo make companion to get full name
                )
                binding.possiblePartnerList.setAdapter(adapter)
            } else {
                Log.d("PartnerListFragment", "No users found.")
            }
        })
    }





    private fun observeTripInvitations() {
        sharedTripViewModel.trip.value?.let { trip ->
            tripViewModel.getTripInvitationsByTripId(trip.id)?.observe(viewLifecycleOwner, Observer { invitations ->
                adapter.submitList(invitations.toMutableList())
            })
        }
    }

    private fun setupRecyclerView() {
        adapter = PartnerListAdapter(userViewModel)
        binding.invitationList.adapter = adapter
        binding.invitationList.layoutManager = LinearLayoutManager(requireContext())

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val invitation = adapter.getInvitationAtPosition(position)
                adapter.removeInvitationAtPosition(position)
                if(invitation.status == TripInvitationStatus.PENDING) {
                    tripViewModel.deleteTripInvitation(invitation)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.invitationList)
    }

}
