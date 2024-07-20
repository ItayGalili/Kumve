package il.co.erg.mykumve.ui.main

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
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
import il.co.erg.mykumve.ui.trip.TripAdapter
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.User
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.databinding.MainScreenBinding
import il.co.erg.mykumve.ui.main.MainActivity.Companion.DEBUG_MODE
import il.co.erg.mykumve.ui.menu.NotificationsFragment
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripWithInfo
import il.co.erg.mykumve.util.TripInvitationStatus
import il.co.erg.mykumve.util.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainScreenManager : Fragment() {
    // Toolbar
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

        binding.reportsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreenManager_to_UsersReports)
        }

        binding.partnersBtnMs.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreenManager_to_networkManager)
        }

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
            mutableListOf(),
            sharedViewModel,
            requireContext(),
            onItemLongClickListener = { tripWithInfo ->
                onTripLongClicked(tripWithInfo)
            },
            lifecycleOwner = viewLifecycleOwner,
        )
        binding.mainRecyclerView.adapter = tripAdapter
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Toolbar
        toolbar = view.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)

        if (UserManager.isLoggedIn()) {
            currentUser = UserManager.getUser()
            currentUser?.let { user ->
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        tripViewModel.fetchTripsByParticipantUserIdWithInfo(currentUser!!.id)
                        tripViewModel.tripsWithInfo.collectLatest { tripsWithInfo ->
                            tripAdapter.updateTripList(tripsWithInfo)
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
                deleteTrip(viewHolder)
            }

            private fun deleteTrip(viewHolder: RecyclerView.ViewHolder) {
                val tripWithInfo = tripAdapter.tripsWithInfo[viewHolder.adapterPosition]
                val trip = tripWithInfo.trip
                Toast.makeText(requireContext(), "Deleting Trip: ${trip.title}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Swipe action, deleting ${trip.title} on ${viewHolder.adapterPosition} index")

                viewLifecycleOwner.lifecycleScope.launch {
                    tripViewModel.deleteTrip(trip)
                    observeDeleteResult(viewHolder)
                }
            }

            private fun observeDeleteResult(viewHolder: RecyclerView.ViewHolder) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        tripViewModel.operationResult
                            .distinctUntilChanged()
                            .collectLatest { resource ->
                                if (resource.status == Status.SUCCESS) {
                                    Log.d(TAG, "Trip deleted successfully.")
                                    tripAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                                } else {
                                    Toast.makeText(requireContext(), "Failed to delete trip: ${resource.message}", Toast.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)

        if (MainActivity.DEBUG_MODE) {
            menu.findItem(R.id.debug_delete_db).isVisible = true
        }

        val menuItem = menu.findItem(R.id.menuAlerts)
        val actionView = menuItem?.actionView
        val badge = actionView?.findViewById<TextView>(R.id.alert_badge_textview)
        actionView?.setOnClickListener {
            onOptionsItemSelected(menuItem)
        }

        observeUserTripInvitations(UserManager.getUser()?.id ?: "")
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

    private fun observeUserTripInvitations(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Fetch trip invitations for the user
            tripViewModel.fetchTripInvitationsForUser(userId)

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tripViewModel.tripInvitations.collectLatest { invitations ->
                    // Handle the trip invitations
                    val menuItem = toolbar.menu.findItem(R.id.menuAlerts)
                    val actionView = menuItem?.actionView
                    val badge = actionView?.findViewById<TextView>(R.id.alert_badge_textview)
                    val pendingInvitations = invitations.filter { it.status == TripInvitationStatus.PENDING }
                    val pendingInvitationsCount = pendingInvitations.size
                    if (pendingInvitationsCount == 0) {
                        badge?.visibility = View.GONE
                    } else {
                        badge?.text = "+$pendingInvitationsCount"
                        badge?.visibility = View.VISIBLE
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
