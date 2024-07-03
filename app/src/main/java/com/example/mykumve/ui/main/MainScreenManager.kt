package com.example.mykumve.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.ui.trip.TripAdapter
import com.example.mykumve.R
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.MainScreenBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.NavigationArgs
import com.example.mykumve.util.UserManager

class MainScreenManager : Fragment() {
    //toolbar
    private lateinit var toolbar: Toolbar
    //toolbar
    private var _binding: MainScreenBinding? = null
    private val binding get() = _binding!!
    private val tripViewModel: TripViewModel by activityViewModels()
    private val sharedViewModel: SharedTripViewModel by activityViewModels()
    private lateinit var tripAdapter: TripAdapter
    private var currentUser: User? = null
    private var _firstTimeShowingScreen = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = MainScreenBinding.inflate(inflater, container, false)

        binding.addBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean(NavigationArgs.IS_CREATING_NEW_TRIP.key, true)
            }
            findNavController().navigate(R.id.action_mainScreenManager_to_travelManager, bundle)
        }

        binding.partnersBtnMs.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreenManager_to_networkManager)
        }

        binding

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tripAdapter = TripAdapter(emptyList(), sharedViewModel)
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
            currentUser?.let {
                if (_firstTimeShowingScreen) {
                    Toast.makeText( // todo remove - only for debug
                        requireContext(),
                        getString(R.string.welcome_user, it.firstName),
                        Toast.LENGTH_SHORT
                    ).show()
                    _firstTimeShowingScreen = false
                }

                tripViewModel.getTripsByUserId(it.id)?.observe(viewLifecycleOwner) { trips ->
                    tripAdapter.trips = trips
                    tripAdapter.notifyDataSetChanged()
                    var welcome_msg=binding.informationWhileEmpty
                    if (tripAdapter.itemCount >0) {
                        welcome_msg.alpha=0f
                    }
                    else{
                        welcome_msg.alpha=1f
                    }

                }

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
                Toast.makeText(
                    requireContext(),
                    "adapterPosition=${viewHolder.adapterPosition}",
                    Toast.LENGTH_SHORT
                ).show()
                tripViewModel.deleteTrip(tripAdapter.trips[viewHolder.adapterPosition])
                tripAdapter.notifyItemRemoved(viewHolder.adapterPosition)


            }

        }).attachToRecyclerView(binding.mainRecyclerView)
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
            R.id.my_alerts -> {
                // todo Handle My Alerts action
                // Example: findNavController().navigate(R.id.action_mainScreenManager_to_myAlerts)
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

    private fun showDeleteDbDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.delete_db_confirmation)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                // delete the database
                UserManager.clearUser()
                requireContext().deleteDatabase("app_database")
                Toast.makeText(requireContext(), "Database deleted. Restarting app...", Toast.LENGTH_SHORT).show()
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}