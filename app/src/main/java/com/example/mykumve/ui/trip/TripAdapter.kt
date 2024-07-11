package com.example.mykumve.ui.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykumve.R
import com.example.mykumve.data.model.Trip
import com.example.mykumve.databinding.TravelCardBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.util.Converters
import com.example.mykumve.util.Utility.toFormattedString

class TripAdapter(
    var trips: List<Trip>,
    private val sharedViewModel: SharedTripViewModel
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    init {
        if (sharedViewModel.isCreatingTripMode) {
            sharedViewModel.equipmentList.observeForever { equipmentList ->
                equipmentList?.let {
                    val tripIndex =
                            trips.indexOfFirst { it.id == sharedViewModel.trip.value?.id }
                    if (tripIndex != -1) {
                        trips[tripIndex].equipment = it.toMutableList()
                        notifyItemChanged(tripIndex)
                    }
                }
            }
        }
    }
    class TripViewHolder(private val binding: TravelCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: Trip, sharedViewModel: SharedTripViewModel) {
            binding.tripTitle.text = trip.title
            binding.areaCard.text = "Dummy area"
            binding.dateCard.text = Converters().toDate(trip.gatherTime?.toString()?.toLong())?.toFormattedString()
            binding.levelCard.text = "Dummy difficulty"

            //image up lode:
            if (trip.image != null) {
                Glide.with(binding.root).load(trip.image).circleCrop()
                    .into(binding.itemImage)
            } else {
                Glide.with(binding.root).load(R.drawable.hills).circleCrop()
                    .into(binding.itemImage)
            }

            binding.participantListCardBtn.setOnClickListener {
                sharedViewModel.selectExistingTrip(trip)
                sharedViewModel.updateEquipment(trip.equipment)
                it.findNavController().navigate(R.id.action_mainScreenManager_to_equipmentFragment)
            }

            binding.partnersCard.setOnClickListener {
                sharedViewModel.selectExistingTrip(trip)
                it.findNavController().navigate(R.id.action_mainScreenManager_to_partnerListFragment)
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
