package com.example.mykumve.ui.notifications

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.FragmentNotificationsBinding
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.launch

class NotificationsFragment : DialogFragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var tripInvitationAdapter: TripInvitationAdapter
    private val tripViewModel: TripViewModel by activityViewModels()
    private lateinit var currentUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripInvitationAdapter = TripInvitationAdapter(emptyList(), tripViewModel, viewLifecycleOwner)
        binding.notificationsList.adapter = tripInvitationAdapter
        binding.notificationsList.layoutManager = LinearLayoutManager(requireContext())

        if (UserManager.isLoggedIn()) {
            UserManager.getUser()?.let { user ->
                observeUserTripInvitations(user.id)
            }
        }
    }

    private fun observeUserTripInvitations(userId: Long) {
        tripViewModel.fetchTripInvitationsForUser(userId) // Ensure this is called to fetch data

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripViewModel.tripInvitations.collect { invitations ->
                    tripInvitationAdapter.updateInvitations(invitations)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        // Calculate the height as half of the screen height
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            screenHeight / 2
        )
    }

}
