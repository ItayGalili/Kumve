package com.example.mykumve.ui.trip
import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykumve.databinding.FragmentPartnerListBinding
import com.example.mykumve.data.model.User
import com.example.mykumve.ui.trip.adapter.PartnerListAdapter
import com.example.mykumve.UserViewModel

class PartnerListFragment : Fragment() {

    private lateinit var binding: FragmentPartnerListBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var adapter: PartnerListAdapter

    private var selectedUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPartnerListBinding.inflate(inflater, container, false)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        setupRecyclerView()
        setupSpinner()
        setupAddPartnerButton()
        setupClosePartnerButton()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = PartnerListAdapter()
        binding.recyclerView2.adapter = adapter
        binding.recyclerView2.layoutManager = LinearLayoutManager(requireContext())

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
                val user = adapter.getUserAtPosition(position)
                adapter.removeUserAtPosition(position)
                userViewModel.deleteUser(user)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView2)
    }

    private fun setupSpinner() {
        userViewModel.getAllUsers()?.observe(viewLifecycleOwner, Observer { users ->
            val userNames = users.map { it.firstName }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinner.adapter = adapter

            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    selectedUser = users[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedUser = null
                }
            }
        })
    }

    private fun setupAddPartnerButton() {
        binding.addPartner.setOnClickListener {
            selectedUser?.let {
                adapter.addUser(it)
            }
        }
    }

    private fun setupClosePartnerButton() {
        binding.closePartnerBtn.setOnClickListener {
            // שמירת הנתונים שנבחרו
            val selectedUsers = adapter.getAllUsers()
            userViewModel.saveSelectedUsers(selectedUsers)

            // סגירת הפרגמנט
            parentFragmentManager.popBackStack()
        }
    }
}
