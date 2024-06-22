package com.example.mykumve.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.UserManager

class MainScreenManager: Fragment(){

    private var _binding : MainScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel : TripViewModel by activityViewModels()
    private lateinit var tripAdapter: TripAdapter
    private var currentUser: User? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainScreenBinding.inflate(inflater,container,false)

        binding.addBtn.setOnClickListener{
            findNavController().navigate(R.id.action_mainScreenManager_to_travelManager)
        }

        binding.partnersBtnMs.setOnClickListener{
            findNavController().navigate(R.id.action_mainScreenManager_to_networkManager)
        }

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripAdapter = TripAdapter(emptyList(), viewModel)
        binding.mainRecyclerView.adapter = tripAdapter
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (UserManager.isLoggedIn()) {
            currentUser = UserManager.getUser()
            currentUser?.let {
                Toast.makeText( //todo only first time
                    requireContext(),
                    getString(R.string.welcome_user, it.firstName),
                    Toast.LENGTH_SHORT
                ).show()

                viewModel.getTripsByUserId(it.id)?.observe(viewLifecycleOwner) { trips ->
                    tripAdapter.trips = trips
                    tripAdapter.notifyDataSetChanged()
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
            ) = makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                tripAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }

        }).attachToRecyclerView(binding.mainRecyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}