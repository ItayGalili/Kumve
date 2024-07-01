package com.example.mykumve.ui.trip

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import com.example.mykumve.util.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.R
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.databinding.EquipmentListBinding
import com.example.mykumve.databinding.EquipmentCardBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel

class EquipmentFragment : Fragment() {

    private var _binding: EquipmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EquipmentAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedTripViewModel: SharedTripViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EquipmentListBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireContext().getSharedPreferences("equipment_prefs", Context.MODE_PRIVATE)
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
//            sharedTripViewModel.updateEquipment(adapter.getEquipmentList())  // Update equipment list in the view model
            findNavController().navigate(R.id.action_equipmentFragment_to_travelManager) //todo bug- should return to the page you came from
        }
    }

    private fun addNewEquipment() {
        val lastPosition = adapter.itemCount - 1
        val lastViewHolder = binding.recyclerView2.findViewHolderForAdapterPosition(lastPosition) as? EquipmentAdapter.EquipmentViewHolder

        lastViewHolder?.binding?.equipmentName?.let {
            val equipmentName = it.text.toString()
            val updatedEquipment = adapter.getEquipmentList()[lastPosition].copy(name = equipmentName)
            adapter.updateEquipment(lastPosition, updatedEquipment)
            it.clearFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        if (adapter.getEquipmentList().isNotEmpty() && adapter.getEquipmentList().lastOrNull()?.name.isNullOrEmpty()) {
            Toast.makeText(context, "Please fill the last equipment item before adding a new one.", Toast.LENGTH_SHORT).show()
        } else {
            val newEquipment = Equipment("", false,  null)
            adapter.addEquipment(newEquipment)
        }
    }

    private fun saveData() {
        val filteredList = adapter.getEquipmentList().filter { it.name.isNotEmpty() }.toMutableList() //don't save empty items
        sharedTripViewModel.updateEquipment(filteredList)
    }

    private fun loadData(): MutableList<Equipment> {
        return sharedTripViewModel.equipmentList.value?.toMutableList() ?: mutableListOf()  // Changed line
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class EquipmentAdapter(private val equipmentList: MutableList<Equipment>) :
    RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding =
            EquipmentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = equipmentList[position]
        holder.bind(equipment, this)
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

    fun updateEquipment(position: Int, updatedEquipment: Equipment) {
        equipmentList[position] = updatedEquipment
        notifyItemChanged(position)
    }

    class EquipmentViewHolder(internal val binding: EquipmentCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(equipment: Equipment, adapter: EquipmentAdapter) {
            UserManager.getUser()?.let {
                val userId = it.id
                val equipmentName = Editable.Factory.getInstance().newEditable(equipment.name)
                if (equipmentName.isBlank()) binding.equipmentName.hint = "New Equipment ${adapter.itemCount}"

                val userFullName = if (equipment.done) "${it.firstName} ${it.surname}" else ""
                binding.equipmentName.text = equipmentName
                binding.nameRes.text = userFullName
                binding.equipmentResponsibility.isChecked = equipment.done

                // Reset the background color based on the done status


                binding.equipmentResponsibility.setOnClickListener {
                    val newDoneStatus = !equipment.done

                    if (newDoneStatus) {
                        binding.nameRes.text = userFullName
                    } else {
                        binding.nameRes.text = ""
                    }
                    val updatedEquipment = equipment.copy(
                        name = binding.equipmentName.text.toString(),
                        done = newDoneStatus,
                        userId = if (newDoneStatus) userId else null
                    )
                    adapter.updateEquipment(adapterPosition, updatedEquipment)
                }
            }
        }

    }
}

class SwipeToDeleteCallback(private val adapter: EquipmentAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

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
