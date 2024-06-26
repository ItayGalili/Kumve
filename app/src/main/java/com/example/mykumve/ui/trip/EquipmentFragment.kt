package com.example.mykumve.ui.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.databinding.EquipmentListBinding
import com.example.mykumve.databinding.EquipmentCardBinding

class EquipmentFragment : Fragment() {

    private var _binding: EquipmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EquipmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView2.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView2.adapter = EquipmentAdapter(getDummyData())

        binding.addEquipmentBtn.setOnClickListener {
            // Handle add equipment button click
        }

        binding.closeEquipmentBtn.setOnClickListener {
            // Handle close button click
        }
    }

    private fun getDummyData(): List<Equipment> {
        return listOf(
            Equipment("Equipment 1", "Responsibility 1"),
            Equipment("Equipment 2", "Responsibility 2"),
            Equipment("Equipment 3", "Responsibility 3")
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Equipment(val name: String, val responsibility: String)

class EquipmentAdapter(private val equipmentList: List<Equipment>) :
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

    class EquipmentViewHolder(private val binding: EquipmentCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(equipment: Equipment) {
            binding.equipmentName.text = equipment.name
            binding.equipmentResponsibility.text = equipment.responsibility

            binding.checkBox.setOnClickListener {
                // Handle checkbox click
            }

            binding.deleteEquipmentBtn.setOnClickListener {
                // Handle delete button click
            }

            binding.editEquipment.setOnClickListener {
                // Handle edit button click
            }
        }
    }
}
