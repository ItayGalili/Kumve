package com.example.mykumve.ui.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.EquipmentListBinding
import com.example.mykumve.databinding.EquipmentCardBinding
import com.example.mykumve.ui.viewmodel.UserViewModel

class EquipmentFragment : Fragment() {

    private var _binding: EquipmentListBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

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
        binding.recyclerView2.adapter = EquipmentAdapter(getDummyData(), userViewModel)

        binding.addEquipmentBtn.setOnClickListener {
            // Handle add equipment button click
        }

        binding.closeEquipmentBtn.setOnClickListener {
            // Handle close button click
        }
    }

    private fun getDummyData(): List<Equipment> {
        return listOf(
            Equipment("Equipment 1", false, 1),
            Equipment("Equipment 2", false,2),
            Equipment("Equipment 3", false,3),
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//data class Equipment(val name: String, val responsibility: String)

class EquipmentAdapter(private val equipmentList: List<Equipment>, var userViewModel: UserViewModel) :
    RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = EquipmentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding, userViewModel = userViewModel )
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = equipmentList[position]
        holder.bind(equipment)
    }

    override fun getItemCount() = equipmentList.size

    class EquipmentViewHolder(private val binding: EquipmentCardBinding,
                              private val userViewModel: UserViewModel) : RecyclerView.ViewHolder(binding.root) {
        fun bind(equipment: Equipment) {
            val user: User?= userViewModel.getUserById(equipment.userId)?.value
            binding.equipmentName.text = equipment.name
            binding.equipmentResponsibility.text = "${user?.firstName} ${user?.surname}" // todo string

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
