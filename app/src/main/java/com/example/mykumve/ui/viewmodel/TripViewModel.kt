package com.example.mykumve.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykumve.data.db.repository.TripRepository
import com.example.mykumve.data.model.Trip
import kotlinx.coroutines.launch

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TripRepository(application)
    private val _trip = MutableLiveData<Trip>()
    val trip: LiveData<Trip> get() = _trip

//    private val _trips = MutableLiveData<List<Trip>>()
    val allTrips : LiveData<List<Trip>>? = repository.getAllTrips()

    fun addTrip(trip: Trip) {
        repository.addTrip(trip)
//        getAllTrips()  // Refresh the list of trips
    }
    fun deleteTrip(trip: Trip) {
        repository.deleteTrip(trip)
//        getAllTrips()  // Refresh the list of trips
    }


}
