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
    private val _selectedExistingTrip = MutableLiveData<Trip?>()
    private lateinit var tripViewModel: TripViewModel
    private val _tempEquipmentList = MutableLiveData<List<Equipment>?>()
    val equipmentList: MutableLiveData<List<Equipment>?> get() = _tempEquipmentList
    var isCreatingTripMode: Boolean = true
    private val _partialTrip = MutableLiveData<Trip?>()
    val trip: LiveData<Trip?> get() = if(isCreatingTripMode) _partialTrip else _selectedExistingTrip
    fun setPartialTrip(trip: Trip) {
        _partialTrip.value = trip
        isCreatingTripMode = true
    }

    fun updatePartialTrip(trip: Trip) {
        _partialTrip.value = _partialTrip.value?.apply {
            this.title = trip.title
            this.description = trip.description
            this.gatherTime = trip.gatherTime
            this.participants = trip.participants
            this.image = trip.image
            this.equipment = trip.equipment
            this.userId = trip.userId
            this.tripInfoId = trip.tripInfoId
            this.notes = trip.notes
            this.endDate = trip.endDate
            this.invitations = trip.invitations
            this.shareLevel = trip.shareLevel
        }
    }


    fun initTripViewModel(viewModelStoreOwner: ViewModelStoreOwner) {
        tripViewModel = ViewModelProvider(viewModelStoreOwner).get(TripViewModel::class.java)
    }

    fun selectExistingTrip(trip: Trip) {
        _selectedExistingTrip.value = trip
        _tempEquipmentList.value = trip.equipment // Load the trip's equipment list when selected
        isCreatingTripMode = false
    }

    fun updateEquipment(equipment: MutableList<Equipment>?) {
        trip.value?.equipment =
            equipment?.toMutableList() // Ensure the trip's equipment list is updated
        if (isCreatingTripMode) {
            _tempEquipmentList.value = equipment
        } else try {
            trip.value?.let {
                tripViewModel.updateTrip(it) // Call TripViewModel to update the trip
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error with update equipment \n${e.message.toString()}")

        }
    }

    fun resetNewTripState() {
        _selectedExistingTrip.value = null
        _partialTrip.value = null
        _tempEquipmentList.value = mutableListOf()
        isCreatingTripMode = true
    }

}
