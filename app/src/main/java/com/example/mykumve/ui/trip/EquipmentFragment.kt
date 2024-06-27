package com.example.mykumve.ui.trip

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.R
import com.example.mykumve.databinding.EquipmentListBinding
import com.example.mykumve.databinding.EquipmentCardBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class EquipmentFragment : Fragment() {

    private var _binding: EquipmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EquipmentAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EquipmentListBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("equipment_prefs", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView2.layoutManager = LinearLayoutManager(requireContext())
        adapter = EquipmentAdapter(loadData())
        binding.recyclerView2.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerView2)

        binding.addEquipmentBtn.setOnClickListener {
            addNewEquipment()
        }

        binding.closeEquipmentBtn.setOnClickListener {
            saveData()
            findNavController().navigate(R.id.action_equipmentFragment_to_travelManager)

        }
    }

    private fun addNewEquipment() {
        val newEquipment = Equipment("New Equipment", "")
        adapter.addEquipment(newEquipment)
    }

    private fun saveData() {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(adapter.getEquipmentList())
        editor.putString("equipment_list", json)
        editor.apply()
    }

    private fun loadData(): MutableList<Equipment> {
        val json = sharedPreferences.getString("equipment_list", null)
        return if (json != null) {
            val type: Type = object : TypeToken<MutableList<Equipment>>() {}.type
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Equipment(val name: String, val responsibility: String)

class EquipmentAdapter(private val equipmentList: MutableList<Equipment>) :
    RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = EquipmentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = equipmentList[position]
        holder.bind(equipment)
    }

    override fun getItemCount() = equipmentList.size

    fun addEquipment(equipment: Equipment) {
        equipmentList.add(equipment)
        notifyItemInserted(equipmentList.size - 1)
    }

    fun removeEquipment(position: Int) {
        equipmentList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getEquipmentList(): MutableList<Equipment> {
        return equipmentList
    }

    class EquipmentViewHolder(private val binding: EquipmentCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(equipment: Equipment) {
            //binding.equipmentName.text = equipment.name
            binding.equipmentResponsibility.text = equipment.responsibility

            binding.equipmentResponsibility.setOnClickListener {
                if (binding.equipmentResponsibility.isChecked) {
                    binding.equipmentResponsibility.isChecked = true
                    binding.nameRes.text = "itay"
                    binding.equipmentResponsibility.setBackgroundColor(Color.parseColor("#8BC34A"))
                    binding.equipmentName.setBackgroundColor(Color.parseColor("#8BC34A"))
                    binding.nameRes.setBackgroundColor(Color.parseColor("#8BC34A"))
                } else {
                    binding.equipmentResponsibility.setBackgroundColor(Color.parseColor("#DE6A6D"))
                    binding.equipmentName.setBackgroundColor(Color.parseColor("#DE6A6D"))
                    binding.nameRes.setBackgroundColor(Color.parseColor("#DE6A6D"))
                    binding.nameRes.text = ""
                }
            }
        }
    }
}

class SwipeToDeleteCallback(private val adapter: EquipmentAdapter) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.removeEquipment(position)
    }
}
