package com.example.mykumve.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.Trip

class SharedTripViewModel : ViewModel() {
    private val _selectedTrip = MutableLiveData<Trip>()
    val selectedTrip: LiveData<Trip> get() = _selectedTrip

    private val _equipmentList = MutableLiveData<List<Equipment>?>()
    val equipmentList: MutableLiveData<List<Equipment>?> get() = _equipmentList

    fun selectTrip(trip: Trip) {
        _selectedTrip.value = trip
        _equipmentList.value = trip.equipment // Load the trip's equipment list when selected
    }

    fun updateEquipment(equipment: MutableList<Equipment>) {
        _equipmentList.value = equipment
        _selectedTrip.value?.equipment = equipment.toMutableList() // Ensure the trip's equipment list is updated
    }
}