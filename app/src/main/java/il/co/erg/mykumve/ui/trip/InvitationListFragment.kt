package il.co.erg.mykumve.ui.trip

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.model.TripInvitation
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.databinding.FragmentInvitationListBinding
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.TripInvitationStatus
import il.co.erg.mykumve.util.UserManager
import il.co.erg.mykumve.util.UserUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch


class InvitationListFragment : Fragment() {
    val TAG = InvitationListFragment::class.java.simpleName

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


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleCloseButton()
        }
        binding.closePartnerBtn.setOnClickListener{
            handleCloseButton()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedTripViewModel.trip.collectLatest { trip ->
                    trip?.invitations?.let { invitations ->
                        invitationListAdapter.updateInvitations(invitations)
                    }
                }
            }
        }
        binding.addPartner.setOnClickListener {
            val phoneNumber = binding.phoneNumberToInvite.text.toString()
            invitePartnerByPhone(phoneNumber)
        }


    }

        private fun handleCloseButton() {
        //            saveData() todo (save to db / cached the removed ones also)
        findNavController().navigate(R.id.action_invitationListFragment_to_partnerListFragment)
    }

    private fun invitePartnerByPhone(phoneNumber: String) {
        Log.d(TAG, "Phone number to invite: $phoneNumber")
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.fetchUserByPhone(phoneNumber)  // Ensure this method updates the flow
                userViewModel.userByPhone
                    .filterNotNull()
                    .distinctUntilChanged()
                    .collectLatest { user ->
                        Log.d(TAG, "User found: ${UserUtils.getFullName(user)} with ID ${user.id}")
                        val currentTrip = sharedTripViewModel.trip.value
                        if (currentTrip != null) {
                            if (sharedTripViewModel.isCreatingTripMode) {
                                val newInvitation = TripInvitation(
                                    tripId = currentTrip.id,
                                    userId = user.id,
                                    status = TripInvitationStatus.UNSENT
                                )
                                val updateInvitationList = (currentTrip.invitations + newInvitation).toMutableList()
                                sharedTripViewModel.updateTrip(currentTrip.copy(invitations = updateInvitationList))
                                Toast.makeText(requireContext(), "Invitation added", Toast.LENGTH_SHORT).show()
                            } else {
                                tripViewModel.sendTripInvitation(
                                    tripId = currentTrip.id,
                                    userId = user.id
                                ) { result ->
                                    when (result.status) {
                                        Status.SUCCESS -> {
                                            Toast.makeText(requireContext(), "Invitation sent and trip updated", Toast.LENGTH_SHORT).show()
                                        }
                                        Status.ERROR -> {
                                            Toast.makeText(requireContext(), "Failed to send invitation: ${result.message}", Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            // Handle loading state if necessary
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "No current trip found.")
                            Toast.makeText(requireContext(), "No current trip found.", Toast.LENGTH_SHORT).show()
                        }
                    }
                userViewModel.userByPhone
                    .filter { user -> user == null }
                    .distinctUntilChanged()
                    .collectLatest {
                        Log.d(TAG, "User not found for phone number: $phoneNumber")
                        Toast.makeText(requireContext(), "Can't find user with this phone number", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun logPossiblePartner() {
        userViewModel.fetchAllUsers() // Ensure this is called to fetch data

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.allUsers.collectLatest { users ->
                    if (users != null) {
                        val currentUserId = UserManager.getUser()?.id
                        val filteredUsers = users.filter { it.id != currentUserId }
                        Log.d(
                            TAG,
                            filteredUsers.map { "${it.firstName}: ${it.phone}" }.toString()
                        )
                    } else {
                        Log.d(TAG, "No users found.")
                    }
                }
            }
        }
    }

    private fun observeTripInvitations() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedTripViewModel.trip.collectLatest { trip ->
                    Log.d(TAG, "observeTripInvitations. invitations: ${trip?.invitations}")
                    Log.d(TAG, "observeTripInvitations. user ids: ${trip?.invitations?.map { it.userId }}")
                    trip?.let { currentTrip ->
                        if (sharedTripViewModel.isCreatingTripMode) {
                            // For temporary trip (partialTrip)
                            invitationListAdapter.submitList(currentTrip.invitations.toMutableList())
                        } else {
                            tripViewModel.fetchTripInvitationsByTripId(currentTrip.id) // Ensure this is called to fetch data
                            tripViewModel.tripInvitations.collectLatest { invitations ->
                                invitationListAdapter.submitList(invitations.toMutableList())
                            }
                        }
                    } ?: run {
                        invitationListAdapter.submitList(mutableListOf())
                    }
                }
            }
        }
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
