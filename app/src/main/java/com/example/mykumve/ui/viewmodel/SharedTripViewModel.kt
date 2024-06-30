package com.example.mykumve.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.Trip

class SharedTripViewModel : ViewModel() {
    val TAG = SharedTripViewModel::class.java.toString()
    private val _selectedTrip = MutableLiveData<Trip?>()
    private lateinit var tripViewModel: TripViewModel
    val selectedTrip: LiveData<Trip?> get() = _selectedTrip
    private val _equipmentList = MutableLiveData<List<Equipment>?>()
    val equipmentList: MutableLiveData<List<Equipment>?> get() = _equipmentList
    var isNewTrip: Boolean = true

    fun initTripViewModel(viewModelStoreOwner: ViewModelStoreOwner) {
        tripViewModel = ViewModelProvider(viewModelStoreOwner).get(TripViewModel::class.java)
    }

    fun selectTrip(trip: Trip) {
        _selectedTrip.value = trip
        _equipmentList.value = trip.equipment // Load the trip's equipment list when selected
        isNewTrip = false
    }

    fun updateEquipment(equipment: MutableList<Equipment>?) {
        selectedTrip.value?.equipment =
            equipment?.toMutableList() // Ensure the trip's equipment list is updated
        if (isNewTrip) {
            _equipmentList.value = equipment
        } else try {
            selectedTrip.value?.let {
                tripViewModel.updateTrip(it) // Call TripViewModel to update the trip
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with update equipment \n${e.message.toString()}")

        }
    }

    fun resetNewTripState() {
        _selectedTrip.value = null
        _equipmentList.value = mutableListOf()
        isNewTrip = true
    }

}
