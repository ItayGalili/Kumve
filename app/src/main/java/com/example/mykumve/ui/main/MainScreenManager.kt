package com.example.mykumve.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.ui.trip.TripAdapter
import com.example.mykumve.R
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.MainScreenBinding
import com.example.mykumve.ui.main.MainActivity.Companion.DEBUG_MODE
import com.example.mykumve.ui.notifications.NotificationsFragment
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.ui.viewmodel.TripWithInfo
import com.example.mykumve.util.TripInvitationStatus
import com.example.mykumve.util.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainScreenManager : Fragment() {
    //toolbar
    private lateinit var toolbar: Toolbar
    private var _binding: MainScreenBinding? = null
    private val binding get() = _binding!!
    private val tripViewModel: TripViewModel by activityViewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var tripAdapter: TripAdapter
    private var currentUser: User? = null
    private var _firstTimeShowingScreen = true
    val TAG = MainScreenManager::class.java.simpleName


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = MainScreenBinding.inflate(inflater, container, false)

        binding.addBtn.setOnClickListener {
            sharedViewModel.resetNewTripState()
            findNavController().navigate(R.id.action_mainScreenManager_to_travelManager)
        }

//        binding.reportsBtn.setOnClickListener {
//            findNavController().navigate(R.id.action_mainScreenManager_to_UsersReports)
//        }
//
//        binding.partnersBtnMs.setOnClickListener {
//            findNavController().navigate(R.id.action_mainScreenManager_to_networkManager)
//        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.mainScreenManager)
        }

        initializeComponent()
        return binding.root
    }

    private fun initializeComponent() {
        sharedViewModel.isEditingExistingTrip = false
        sharedViewModel.isCreatingTripMode = false

    }


    private fun clearFragmentBackStack() {
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tripAdapter = TripAdapter(
            emptyList(),
            sharedViewModel,
            requireContext(),
            onItemLongClickListener = { tripWithInfo ->
                onTripLongClicked(tripWithInfo)
            },
            lifecycleOwner = viewLifecycleOwner,
        )
        binding.mainRecyclerView.adapter = tripAdapter
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        //toolbar
        toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        // Ensure menu items are displayed in the Toolbar
        setHasOptionsMenu(true)
        //toolbar


        if (UserManager.isLoggedIn()) {
            currentUser = UserManager.getUser()
            currentUser?.let { user ->
                if (_firstTimeShowingScreen) {
                    Toast.makeText( // todo remove - only for debug
                        requireContext(),
                        getString(R.string.welcome_user, user.firstName),
                        Toast.LENGTH_SHORT
                    ).show()
                    _firstTimeShowingScreen = false
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        tripViewModel.fetchTripsByParticipantUserIdWithInfo(currentUser!!.id)
                        tripViewModel.tripsWithInfo.collectLatest { tripsWithInfo ->
                            tripAdapter.tripsWithInfo = tripsWithInfo
                            tripAdapter.notifyDataSetChanged()
                            val welcomeMsg = binding.informationWhileEmpty
                            welcomeMsg.alpha = if (tripAdapter.itemCount > 0) 0f else 1f
                            if (DEBUG_MODE) {
                                logTripsWithInfo(tripsWithInfo)
                            }
                        }
                    }
                }

                // Observe trip invitations
                observeUserTripInvitations(user.id)
            }

        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(requireContext(), R.string.please_log_in, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_registerManager_to_loginManager)
        }

        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) = makeFlag(
                ItemTouchHelper.ACTION_STATE_SWIPE,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val tripWithInfo = tripAdapter.tripsWithInfo[viewHolder.adapterPosition]
                val trip = tripWithInfo.trip
                Toast.makeText(
                    requireContext(),
                    "Deleting Trip: ${trip.title}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(
                    TAG, "Swipe action, deleting ${trip.title} " +
                            "on ${viewHolder.adapterPosition} index"
                )

                tripViewModel.deleteTrip(trip)

                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        tripViewModel.operationResult.collectLatest { result ->
                            if (result?.success == true) {
                                // Re-fetch the trip list to ensure the UI is updated correctly
                                tripViewModel.fetchAllTrips()
                                tripAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                            } else {
                                // Handle deletion failure, e.g., show a Toast or Snackbar
                                Toast.makeText(
                                    requireContext(),
                                    "Failed to delete trip: ${result?.reason}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Notify the adapter that the item has not been removed
                                tripAdapter.notifyItemChanged(viewHolder.adapterPosition)
                            }
                        }
                    }
                }
            }
        }).attachToRecyclerView(binding.mainRecyclerView)
        Log.d(TAG, "Creating mode: ${sharedViewModel.isCreatingTripMode}\nEditing mode: ${sharedViewModel.isEditingExistingTrip}")
    }

    private fun logTripsWithInfo(tripsWithInfo: List<TripWithInfo>) {
        tripsWithInfo.forEach { tripWithInfo ->
            val trip = tripWithInfo.trip
            val tripInfo = tripWithInfo.tripInfo
            val detailedTripInfo = tripInfo?.let {
                """
            |Trip Info:
            |    ID: ${it.id}
            |    Title: ${it.title}
            |    Points: ${it.points}
            |    Area ID: ${it.areaId}
            |    Sub Area ID: ${it.subAreaId}
            |    Description: ${it.description}
            |    Route Description: ${it.routeDescription}
            |    Difficulty: ${it.difficulty}
            |    Trip ID: ${it.tripId}
            """.trimMargin()
            } ?: "No trip info available"

            val prettyTrip = """
        |Trip
        | id: ${trip.id},
        |    title: ${trip.title},
        |    description: ${trip.description},
        |    gatherTime: ${trip.gatherTime},
        |    tripInfoId: ${trip.tripInfoId},
        |    endDate: ${trip.endDate},
        |    participants: ${trip.participants?.size},
        |    equipment: ${trip.equipment?.size},
        |    invitations: ${trip.invitations.size},
        |    shareLevel: ${trip.shareLevel}
        |)
        |$detailedTripInfo
        """.trimMargin()

            Log.d(TAG, prettyTrip)
        }
    }



    //toolbar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
        if (MainActivity.DEBUG_MODE) {
            menu.findItem(R.id.debug_delete_db).isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.my_profile -> {
                findNavController().navigate(R.id.action_mainScreenManager_to_myProfile)
                return true
            }

            R.id.menuAlerts -> {
                showNotificationsFragment()
                return true
            }

            R.id.log_out -> {
                showLogoutDialog()
                return true
            }

            R.id.debug_delete_db -> {
                showDeleteDbDialog()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun observeUserTripInvitations(userId: Long) {
        tripViewModel.fetchTripInvitationsForUser(userId) // Ensure this is called to fetch data

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripViewModel.tripInvitations.collectLatest { invitations ->
                    // Handle the trip invitations
                    val pendingInvitations =
                        invitations.filter { it.status == TripInvitationStatus.PENDING }
                    val pendingInvitationsCount = pendingInvitations.size
                    if (pendingInvitationsCount == 0) {
                        Toast.makeText(requireContext(), "No new invitations", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        // Show notifications or update UI with the invitations
                        Toast.makeText(
                            requireContext(),
                            "You have $pendingInvitationsCount new invitations",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun showNotificationsFragment() {
        val notificationsFragment = NotificationsFragment()
        notificationsFragment.show(parentFragmentManager, notificationsFragment.tag)
    }


    private fun showDeleteDbDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.delete_db_confirmation)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                // delete the database
                UserManager.clearUser()
                requireContext().deleteDatabase("kumve_db")
                Toast.makeText(
                    requireContext(),
                    "Database deleted. Restarting app...",
                    Toast.LENGTH_SHORT
                ).show()
                // restart the app
                val intent = requireActivity().intent
                requireActivity().finish()
                startActivity(intent)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes) { dialog, id ->
                UserManager.clearUser()
                findNavController().navigate(R.id.action_mainScreenManager_to_loginManager)
            }
            .setNegativeButton(R.string.no) { dialog, id ->
                dialog.dismiss()
            }

        // Create and show the AlertDialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

//    private fun setAlertsBadgeCount(count: Int) {
//        val menuItem = toolbar.menu.findItem(R.id.menuAlerts)
//        val actionView = menuItem?.actionView ?: return
//
//        val badge = actionView.findViewById<TextView>(R.id.badge)
//
//        if (count > 0) {
//            badge.text = "+$count"
//            badge.visibility = View.VISIBLE
//        } else {
//            badge.visibility = View.GONE
//        }
//    }

    private fun onTripLongClicked(tripWithInfo: TripWithInfo) {
        Toast.makeText(requireContext(), "Long-clicked on: ${tripWithInfo.trip.title}", Toast.LENGTH_SHORT).show()

        sharedViewModel.selectExistingTripWithInfo(tripWithInfo)
        Log.v(TAG, "Navigating to travelManager with trip: ${tripWithInfo.trip.title}, ${tripWithInfo.trip.id}")

        findNavController().navigate(R.id.action_mainScreenManager_to_travelManager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}