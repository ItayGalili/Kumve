package com.example.mykumve.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.ItemAdapter
import com.example.mykumve.ItemManager
import com.example.mykumve.R
import com.example.mykumve.databinding.MainScreenBinding

class MainScreenManager: Fragment(){

    private var _binding : MainScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        binding.mainRecyclerView.adapter = ItemAdapter(ItemManager.items)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            // Handling of special events while touching RecycleView

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) = makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)


            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            // delete travel item:
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                ItemManager.remove(viewHolder.adapterPosition)
                //Refreshing the view on a change in the recycler:
                binding.mainRecyclerView.adapter!!.notifyItemRemoved(viewHolder.adapterPosition)
            }


        }).attachToRecyclerView(binding.mainRecyclerView)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}