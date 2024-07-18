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
import com.example.mykumve.databinding.TravelCardBinding
import com.example.mykumve.ui.viewmodel.SharedTripViewModel
import com.example.mykumve.ui.viewmodel.TripWithInfo
import com.example.mykumve.util.Converters
import com.example.mykumve.util.TripInfoUtils.mapAreaToString
import com.example.mykumve.util.TripInfoUtils.mapDifficultyToString
import com.example.mykumve.util.Utility.toFormattedString
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TripAdapter(
    var tripsWithInfo: List<TripWithInfo>,
    private val sharedViewModel: SharedTripViewModel,
    var context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onItemLongClickListener: ((TripWithInfo) -> Unit)? = null
) : RecyclerView.Adapter<TripAdapter.TripWithInfoViewHolder>() {

    val TAG = TripAdapter::class.java.simpleName

    init {
        if (sharedViewModel.isCreatingTripMode) {
            lifecycleOwner.lifecycleScope.launch {
                lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    sharedViewModel.trip.collectLatest { trip ->
                        trip?.let {
                            val updatedList = tripsWithInfo.map {
                                if (it.trip.id == trip.id) TripWithInfo(trip, it.tripInfo) else it
                            }
                            tripsWithInfo = updatedList
                            notifyItemChanged(tripsWithInfo.indexOfFirst { it.trip.id == trip.id })
                        }
                    }
                }
            }
        }
    }

    inner class TripWithInfoViewHolder(private val binding: TravelCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val TAG = TripWithInfoViewHolder::class.java.simpleName

        fun bind(tripWithInfo: TripWithInfo, sharedViewModel: SharedTripViewModel) {
            val trip = tripWithInfo.trip
            val tripInfo = tripWithInfo.tripInfo
            Log.d(TAG, "Binding trip ${trip.title}, with total ${trip.invitations.size} invitations and ${trip.participants?.size} participants")
            binding.tripTitle.text = trip.title
            binding.areaCard.text = mapAreaToString(context, tripInfo?.subAreaId)
            binding.dateCard.text = Converters().toDate(trip.gatherTime?.toString()?.toLong())?.toFormattedString()
            binding.difficultyCard.text = mapDifficultyToString(context, tripInfo?.difficulty)

            //image up load:
            if (trip.image != null) {
                Glide.with(binding.root).load(trip.image).circleCrop()
                    .into(binding.itemImage)
            } else {
                Glide.with(binding.root).load(R.drawable.hills).circleCrop()
                    .into(binding.itemImage)
            }

            binding.participantListCardBtn.setOnClickListener {
                sharedViewModel.isNavigatedFromTripList = true
                sharedViewModel.selectExistingTripWithInfo(tripWithInfo)
                it.findNavController().navigate(R.id.action_mainScreenManager_to_equipmentFragment)
            }

            binding.partnersCard.setOnClickListener {
                sharedViewModel.isNavigatedFromTripList = true
                sharedViewModel.selectExistingTripWithInfo(tripWithInfo)
                it.findNavController().navigate(R.id.action_mainScreenManager_to_partnerListFragment)
            }

            itemView.setOnLongClickListener {
                sharedViewModel.isNavigatedFromTripList = true
                onItemLongClickListener?.invoke(tripWithInfo)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripWithInfoViewHolder {
        val binding = TravelCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripWithInfoViewHolder(binding)
    }

    override fun getItemCount() = tripsWithInfo.size

    override fun onBindViewHolder(holder: TripWithInfoViewHolder, position: Int) {
        holder.bind(tripsWithInfo[position], sharedViewModel)
    }
}
