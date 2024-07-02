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

class PartnerListFragment : Fragment() {

    private lateinit var binding: FragmentPartnerListBinding
    private lateinit var adapter: PartnerListAdapter

    private var selectedUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPartnerListBinding.inflate(inflater, container, false)

        setupRecyclerView()

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
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.recyclerView2)
    }

}
