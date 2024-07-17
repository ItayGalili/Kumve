package com.example.mykumve.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.mykumve.data.data_classes.Equipment
import com.example.mykumve.data.model.Trip
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.data.model.TripInvitation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedTripViewModel : ViewModel() {
    val TAG = SharedTripViewModel::class.java.simpleName

    private val _selectedExistingTrip = MutableStateFlow<Trip?>(null)
    private val _selectedExistingTripInfo = MutableStateFlow<TripInfo?>(null)
    private lateinit var tripViewModel: TripViewModel
    var isCreatingTripMode: Boolean = false
    var isEditingExistingTrip: Boolean = false

    private val _partialTrip = MutableStateFlow<Trip?>(null)
    private val _partialTripInfo = MutableStateFlow<TripInfo?>(null)
    val trip: StateFlow<Trip?> get() = if (isCreatingTripMode) _partialTrip.asStateFlow() else _selectedExistingTrip.asStateFlow()
    val tripInfo: StateFlow<TripInfo?> get() = if (isCreatingTripMode) _partialTripInfo.asStateFlow() else _selectedExistingTripInfo.asStateFlow()

    fun setPartialTrip(trip: Trip) {
        if (_partialTrip.value != trip) {
            Log.v(TAG, "Setting partial trip ${trip.title} ${trip.id}")
            _partialTrip.value = trip
        } else {
            Log.v(TAG, "_partialTrip and trip is the same ${trip.title} ${trip.id}")
        }
    }

    fun addInvitation(invitation: TripInvitation) {
        _partialTrip.value?.invitations?.add(invitation)
        _partialTrip.value = _partialTrip.value // Notify observers of the change
    }

    fun initTripViewModel(viewModelStoreOwner: ViewModelStoreOwner) {
        tripViewModel = ViewModelProvider(viewModelStoreOwner).get(TripViewModel::class.java)
    }

    fun selectExistingTrip(trip: Trip) {
        if (_selectedExistingTrip.value != trip) {
            Log.v(TAG, "Selecting existing trip. title: ${trip.title}, id: ${trip.id}")
            _selectedExistingTrip.value = trip
        } else {
            Log.v(TAG, "Not selecting same existing trip. title: ${trip.title}, id: ${trip.id}")
        }
    }

    fun selectExistingTripInfo(tripInfo: TripInfo) {
        if (_selectedExistingTripInfo.value != tripInfo) {
            Log.v(TAG, "Selecting existing tripInfo ${tripInfo.title} ${tripInfo.id}")
            _selectedExistingTripInfo.value = tripInfo
        } else {
            Log.v(TAG, "_selectedExistingTripInfo and trip is the same ${tripInfo.title} ${tripInfo.id}")
        }
    }

    fun updateEquipment(equipment: MutableList<Equipment>?) {
        trip.value?.equipment = equipment?.toMutableList() // Ensure the trip's equipment list is updated
        if (!isCreatingTripMode) {
            try {
                trip.value?.let {
                    tripViewModel.updateTrip(it) // Call TripViewModel to update the trip
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error with update equipment \n${e.message}")
            }
        }
    }

    fun resetNewTripState() {
        if (!isEditingExistingTrip) {
            Log.d(TAG, "Resetting new trip state: ${_selectedExistingTrip.value?.title}")
            _selectedExistingTrip.value = null
            _selectedExistingTripInfo.value = null
            _partialTrip.value = null
            _partialTripInfo.value = null
            isCreatingTripMode = true
        }
    }
}
