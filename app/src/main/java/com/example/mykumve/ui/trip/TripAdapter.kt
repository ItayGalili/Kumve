package com.example.mykumve.ui.trip

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykumve.R
import com.example.mykumve.data.model.Trip
import com.example.mykumve.databinding.TravelCardBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.util.Converters
import com.example.mykumve.util.Utility.toFormattedString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TripAdapter(
    var trips: List<Trip>,
    private val sharedViewModel: SharedTripViewModel,
    var context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onItemLongClickListener: ((Trip) -> Unit)? = null
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    val TAG = TripAdapter::class.java.simpleName

    init {
        if (sharedViewModel.isCreatingTripMode) {
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    sharedViewModel.trip.collectLatest { trip ->
                        trip?.equipment?.let {
                            val tripIndex = trips.indexOfFirst { it.id == trip.id }
                            if (tripIndex != -1) {
                                trips[tripIndex].equipment = it.toMutableList()
                                notifyItemChanged(tripIndex)
                            }
                        }
                    }
                }
            }
        }
    }
    inner class TripViewHolder(private val binding: TravelCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val TAG = TripViewHolder::class.java.simpleName

        fun bind(trip: Trip, sharedViewModel: SharedTripViewModel) {
            Log.d(TAG, "Binding trip ${trip.title}" +
                    ", with total ${trip.invitations.size} invitations and  ${trip.participants?.size} participants")
            binding.tripTitle.text = trip.title
            binding.areaCard.text = "Dummy area"
            binding.dateCard.text = Converters().toDate(trip.gatherTime?.toString()?.toLong())?.toFormattedString()
            binding.levelCard.text = "Dummy difficulty"

            //image up load:
            if (trip.image != null) {
                Glide.with(binding.root).load(trip.image).circleCrop()
                    .into(binding.itemImage)
            } else {
                Glide.with(binding.root).load(R.drawable.hills).circleCrop()
                    .into(binding.itemImage)
            }

            sharedViewModel.selectExistingTrip(trip)
//            sharedViewModel.updateEquipment(trip.equipment)
            binding.participantListCardBtn.setOnClickListener {
                it.findNavController().navigate(R.id.action_mainScreenManager_to_equipmentFragment)
            }

            binding.partnersCard.setOnClickListener {
                it.findNavController().navigate(R.id.action_mainScreenManager_to_partnerListFragment)
            }

            itemView.setOnLongClickListener {
                onItemLongClickListener?.invoke(trip)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = TravelCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun getItemCount() = trips.size

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position], sharedViewModel)
    }
}
