package il.co.erg.mykumve.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import il.co.erg.mykumve.data.data_classes.Equipment
import il.co.erg.mykumve.data.db.model.Trip
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SharedTripViewModel : ViewModel() {
    var isExactlyOneTripIsChecked = false
    val TAG = SharedTripViewModel::class.java.simpleName

    private lateinit var tripViewModel: TripViewModel
    var isCreatingTripMode: Boolean = false //true when I want to create a brand new trip
    var isEditingExistingTrip: Boolean = false //true when I longpress an existing trip
    var isNavigatedFromTripList: Boolean = false //true when I press on the icons of the existing trip item
    var isNavigatedFromExplore: Boolean = false //true when I already chose a route and now I want to
                                                // fill other details about my trip - for the first time ever
                                                // (like a brand new trip by with no "next" button, replaced with save)

    private val _selectedExistingTripInfo = MutableStateFlow<TripInfo?>(null)
    private val _selectedExistingTripWithInfo = MutableStateFlow<TripWithInfo?>(null)
    private val _partialTrip = MutableStateFlow<Trip?>(null)
    private val _partialTripInfo = MutableStateFlow<TripInfo?>(null)

    private val _operationResult = MutableSharedFlow<Resource<Void?>>()
    val operationResult: SharedFlow<Resource<Void?>> = _operationResult

    val trip: StateFlow<Trip?>
        get() = if (isCreatingTripMode) _partialTrip.asStateFlow() else _selectedExistingTripWithInfo.value?.trip?.let {
            MutableStateFlow(
                it
            ).asStateFlow()
        } ?: MutableStateFlow(null).asStateFlow()
    val tripInfo: StateFlow<TripInfo?>
        get() {
            return if (isCreatingTripMode) {
                _partialTripInfo.asStateFlow()
            } else if (isNavigatedFromExplore) {
                _selectedExistingTripInfo.value?.let {
                    MutableStateFlow(
                        it
                    ).asStateFlow()
                } ?: MutableStateFlow(null).asStateFlow()
            } else _selectedExistingTripWithInfo.value?.tripInfo?.let {
                MutableStateFlow(
                    it
                ).asStateFlow()
            } ?: MutableStateFlow(null).asStateFlow()
        }


    fun setPartialTrip(trip: Trip) {
        if (_partialTrip.value != trip) {
            Log.v(TAG, "Setting partial trip ${trip.title} ${trip.id}")
            _partialTrip.value = trip
        } else {
            Log.v(TAG, "_partialTrip and trip is the same ${trip.title} ${trip.id}")
        }
    }

    fun setPartialTripInfo(tripInfo: TripInfo) {
        if (_partialTripInfo.value != tripInfo) {
            Log.v(TAG, "Setting partial trip info ${tripInfo.title} ${tripInfo.id}")
            _partialTripInfo.value = tripInfo
        } else {
            Log.v(
                TAG,
                "_partialTripInfo and trip info is the same ${tripInfo.title} ${tripInfo.id}"
            )
        }
    }

    fun initTripViewModel(viewModelStoreOwner: ViewModelStoreOwner) {
        tripViewModel = ViewModelProvider(viewModelStoreOwner).get(TripViewModel::class.java)
    }


    fun selectTripInfo(tripInfo: TripInfo){
        if(isNavigatedFromExplore){
            if (_selectedExistingTripInfo.value!=tripInfo){
                Log.d(
                    TAG,
                    "Selecting existing trip from Explore. title:" +
                            " ${tripInfo.title}, id: ${tripInfo.id}"
                )
                _selectedExistingTripInfo.value = tripInfo
            }
            else {
                Log.v(
                TAG,
                "Not selecting same trip from explorer. title:" +
                        " ${tripInfo.title}, id: ${tripInfo.id}"
            )
            }
        }
    }
    fun selectExistingTripWithInfo(tripWithInfo: TripWithInfo) {
        isEditingExistingTrip = true
        if (_selectedExistingTripWithInfo.value != tripWithInfo) {
            Log.d(
                TAG,
                "Selecting existing trip with info. title: ${tripWithInfo.trip.title}, id: ${tripWithInfo.trip.id}"
            )
            _selectedExistingTripWithInfo.value = tripWithInfo
        } else {
            Log.v(
                TAG,
                "Not selecting same existing trip with info. title: ${tripWithInfo.trip.title}, id: ${tripWithInfo.trip.id}"
            )
        }
    }

    fun updateTrip(updatedTrip: Trip) {
        viewModelScope.launch {
            try {
                if (isCreatingTripMode) {
                    _partialTrip.emit(updatedTrip)
                } else {
                    _selectedExistingTripWithInfo.value?.trip = updatedTrip
                    _selectedExistingTripWithInfo.emit(_selectedExistingTripWithInfo.value)
                }
                _operationResult.emit(Resource.success(null))
            } catch (e: Exception) {
                Log.e(TAG, "Error with updating trip \n${e.message}")
                _operationResult.emit(Resource.error("Error with updating trip: ${e.message}", null))
            }
        }
    }

    fun updateEquipment(equipment: List<Equipment>?) {
        viewModelScope.launch {
            try {
                trip.collectLatest { currentTrip ->
                    if (currentTrip != null) {
                        val updatedTrip = currentTrip.copy(equipment = equipment?.toMutableList())
                        if (!isCreatingTripMode) {
                            tripViewModel.updateTrip(updatedTrip)
                            _operationResult.emit(Resource.success(null))
                        } else {
                            currentTrip.equipment = equipment?.toMutableList() // Ensure the trip's equipment list is updated
                            _operationResult.emit(Resource.success(null))
                        }
                    } else {
                        _operationResult.emit(Resource.error("Trip is null", null))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error with update equipment \n${e.message}")
                _operationResult.emit(Resource.error("Error with update equipment: ${e.message}", null))
            }
        }
    }

    fun resetNewTripState() {
        if (!isEditingExistingTrip) {
            Log.d(
                TAG,
                "Resetting new trip state: ${_selectedExistingTripWithInfo.value?.trip?.title}"
            )
            _selectedExistingTripWithInfo.value = null
            _partialTrip.value = null
            _partialTripInfo.value = null
            isCreatingTripMode = true
        }
    }
}
