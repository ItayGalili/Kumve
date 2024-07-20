package il.co.erg.mykumve.ui.trip

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import il.co.erg.mykumve.util.UserManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.data_classes.Equipment
import il.co.erg.mykumve.databinding.EquipmentListBinding
import il.co.erg.mykumve.databinding.EquipmentCardBinding
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.util.UserUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class EquipmentFragment : Fragment() {

    private var _binding: EquipmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EquipmentAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedTripViewModel: SharedTripViewModel by activityViewModels()
    private val TAG = EquipmentFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = EquipmentListBinding.inflate(inflater, container, false)
        sharedTripViewModel.initTripViewModel(this) // Initialize TripViewModel
        sharedPreferences = requireContext().getSharedPreferences("equipment_prefs", Context.MODE_PRIVATE)
        Log.d(TAG, "onCreateView: View created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.invitationList.layoutManager = LinearLayoutManager(requireContext())
        adapter = EquipmentAdapter(mutableListOf()) // Initialize with an empty list
        binding.invitationList.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(binding.invitationList)

        binding.addEquipmentBtn.setOnClickListener {
            addNewEquipment()
        }

        binding.closeEquipmentBtn.setOnClickListener {
            handleCloseButton()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            handleCloseButton()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loadTripData()
        }
        Log.d(TAG, "Creating mode: ${sharedTripViewModel.isCreatingTripMode}" +
                "\nEditing mode: ${sharedTripViewModel.isEditingExistingTrip}" +
                "\nNavigated From Trip List mode: ${sharedTripViewModel.isNavigatedFromTripList}")
    }

    private suspend fun loadTripData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedTripViewModel.trip.collectLatest { trip ->
                    if (trip != null) {
                            trip.equipment?.let { equipmentList ->
                            Log.d(TAG, "loadTripData: Equipment list loaded with size ${equipmentList.size}")
                            adapter.updateEquipmentList(equipmentList.toMutableList())
                        } ?: Log.e(TAG, "loadTripData: Equipment list is null or empty")
                    }
                    else Log.e(TAG, "loadTripData: trip is null or empty")
                }
            }
        }
    }

    private fun handleCloseButton() {
        if (saveCurrentEditedItem()) {
            saveData()
            if (sharedTripViewModel.isNavigatedFromTripList) {
                sharedTripViewModel.resetNewTripState()
                findNavController().navigate(R.id.action_equipmentFragment_to_mainScreenManager)
            } else {
                findNavController().navigate(R.id.action_equipmentFragment_to_travelManager)
            }
        }
    }

    private fun addNewEquipment() {
        if (saveCurrentEditedItem()) {
            val newEquipment = Equipment()
            val position = adapter.addEquipment(newEquipment)
            binding.invitationList.post {
                binding.invitationList.scrollToPosition(position) // Scroll to the new item
                val lastViewHolder = binding.invitationList.findViewHolderForAdapterPosition(position) as? EquipmentAdapter.EquipmentViewHolder
                lastViewHolder?.binding?.equipmentName?.requestFocus()
            }
            Log.d(TAG, "addNewEquipment: New equipment added at position $position")
        }
    }

    private fun saveCurrentEditedItem(): Boolean {
        val lastPosition = adapter.itemCount - 1
        val lastViewHolder = binding.invitationList.findViewHolderForAdapterPosition(lastPosition) as? EquipmentAdapter.EquipmentViewHolder

        lastViewHolder?.binding?.equipmentName?.let {
            val equipmentName = it.text.toString()
            val updatedEquipment = adapter.getEquipmentList()[lastPosition].copy(name = equipmentName)
            adapter.updateEquipment(lastPosition, updatedEquipment)
            it.clearFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
            Log.d(TAG, "saveCurrentEditedItem: Equipment item updated at position $lastPosition with name $equipmentName")
        }

        return if (adapter.getEquipmentList().isNotEmpty() && adapter.getEquipmentList().lastOrNull()?.name.isNullOrEmpty()) {
            Toast.makeText(context, "Please fill the last equipment item.", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun saveData() {
        val filteredList = adapter.getEquipmentList().filter { it.name.isNotEmpty() }.toMutableList() // Don't save empty items

        viewLifecycleOwner.lifecycleScope.launch {
            sharedTripViewModel.updateEquipment(filteredList)
            sharedTripViewModel.operationResult
                .distinctUntilChanged()
                .collectLatest { result ->
                    result.let {
                        Log.d(TAG, "saveData: Equipment data saved with result: ${it.message}")
                    }
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(TAG, "onDestroyView: View destroyed")
    }
}

class EquipmentAdapter(private val equipmentList: MutableList<Equipment>) :
    RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    private val TAG = EquipmentAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val binding = EquipmentCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EquipmentViewHolder(binding)
    }

    fun updateEquipmentList(newEquipmentList: MutableList<Equipment>) {
        equipmentList.clear()
        equipmentList.addAll(newEquipmentList)
        notifyDataSetChanged()
        Log.d(TAG, "updateEquipmentList: Equipment list updated with size ${equipmentList.size}")
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = equipmentList[position]
        holder.bind(equipment, this)
    }

    override fun getItemCount() = equipmentList.size

    fun addEquipment(equipment: Equipment): Int {
        equipmentList.add(equipment)
        val position = equipmentList.size - 1
        notifyItemInserted(equipmentList.size - 1)
        Log.d(TAG, "addEquipment: Equipment added at position $position")
        return position
    }

    fun removeEquipment(position: Int) {
        equipmentList.removeAt(position)
        notifyItemRemoved(position)
        Log.d(TAG, "removeEquipment: Equipment removed from position $position")
    }

    fun getEquipmentList(): MutableList<Equipment> {
        return equipmentList
    }

    fun updateEquipment(position: Int, updatedEquipment: Equipment) {
        equipmentList[position] = updatedEquipment
        notifyItemChanged(position)
        Log.d(TAG, "updateEquipment: Equipment updated at position $position with name ${updatedEquipment.name}")
    }

    class EquipmentViewHolder(internal val binding: EquipmentCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val TAG = EquipmentViewHolder::class.java.simpleName

        fun bind(equipment: Equipment, adapter: EquipmentAdapter) {
            UserManager.getUser()?.let {
                val userId = it.id
                val equipmentName = Editable.Factory.getInstance().newEditable(equipment.name)
                if (equipmentName.isBlank()) binding.equipmentName.hint = "New Equipment ${adapter.itemCount}"

                val userFullName = if (equipment.done) UserUtils.getFullName(it) else ""
                binding.equipmentName.text = equipmentName
                binding.nameRes.text = userFullName
                binding.equipmentResponsibility.isChecked = equipment.done

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
                    Log.d(TAG, "bind: Equipment responsibility toggled for item at position $adapterPosition")
                }
            }
        }
    }
}

class SwipeToDeleteCallback(private val adapter: EquipmentAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val TAG = SwipeToDeleteCallback::class.java.simpleName

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
        Log.d(TAG, "onSwiped: Equipment swiped and removed at position $position")
    }
}
