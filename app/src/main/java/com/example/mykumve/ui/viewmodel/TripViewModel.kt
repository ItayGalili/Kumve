package com.example.mykumve.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mykumve.data.model.TripInfo
import com.example.mykumve.data.db.repository.TripInfoRepository
import com.example.mykumve.data.db.repository.TripRepository
import com.example.mykumve.data.model.Trip
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class TripViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private var tripRepository: TripRepository = TripRepository(application)
    private var tripInfoRepository: TripInfoRepository = TripInfoRepository(application)

    private val _trip = MutableLiveData<Trip>()
    val trip: LiveData<Trip> get() = _trip

    private val _tripInfo = MutableLiveData<TripInfo>()
    val tripInfo: LiveData<TripInfo> get() = _tripInfo

    private val _trips = MutableLiveData<List<Trip>>()
    val trips: LiveData<List<Trip>> get() = _trips

    fun getTripById(id: Int) {
        viewModelScope.launch {
            _trip.postValue(tripRepository.getTripById(id)?.value)
        }
    }

    fun getTripInfoByTripId(tripId: Int) {
        viewModelScope.launch {
            val trip = tripRepository.getTripById(tripId)?.value
            trip?.tripInfoId?.let {
                _tripInfo.postValue(tripInfoRepository.getTripInfoById(it)?.value)
            }
        }
    }

    fun getTripInfoById(id: Int) {
        viewModelScope.launch {
            _tripInfo.postValue(tripInfoRepository.getTripInfoById(id)?.value)
        }
    }

    fun getAllTrips(): LiveData<List<Trip>>? {
        return tripRepository.getAllTrips()
    }

    fun addTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.insertTrip(trip)
        }
    }

    fun insertTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            tripInfoRepository.insertTripInfo(tripInfo)
        }
    }

    fun updateTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.updateTrip(trip)
        }
    }

    fun updateTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            tripInfoRepository.updateTripInfo(tripInfo)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            tripRepository.deleteTrip(trip)
        }
    }

    fun deleteTripInfo(tripInfo: TripInfo) {
        viewModelScope.launch {
            tripInfoRepository.deleteTripInfo(tripInfo)
        }
    }

    fun getTripsByUserId(userId: Int): LiveData<List<Trip>>? {
        return tripRepository.getTripsByUserId(userId)
    }

}
