package il.co.erg.mykumve.ui.menu

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
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.databinding.FragmentNotificationsBinding
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsFragment : DialogFragment() {
    private val userViewModel: UserViewModel by activityViewModels()

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

        tripInvitationAdapter = TripInvitationAdapter(emptyList(), tripViewModel, userViewModel, viewLifecycleOwner)
        binding.notificationsList.adapter = tripInvitationAdapter
        binding.notificationsList.layoutManager = LinearLayoutManager(requireContext())

        if (UserManager.isLoggedIn()) {
            UserManager.getUser()?.let { user ->
                observeUserTripInvitations(user.id)
            }
        }
    }

    private fun observeUserTripInvitations(userId: String) {
        tripViewModel.fetchTripInvitationsForUser(userId) // Ensure this is called to fetch data

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripViewModel.tripInvitations.collectLatest { invitations ->
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
