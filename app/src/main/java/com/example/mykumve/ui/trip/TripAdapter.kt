package com.example.mykumve.ui.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykumve.data.model.Trip
import com.example.mykumve.databinding.TravelCardBinding
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.Converters
import com.example.mykumve.util.Utility.toFormattedString

class TripAdapter(var trips: List<Trip>, private val viewModel: TripViewModel) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    class TripViewHolder(private val binding: TravelCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: Trip, viewModel: TripViewModel) {
            binding.tripTitle.text = trip.title
            binding.areaCard.text = "Dummy area"
            binding.dateCard.text = Converters().toDate(trip.gatherTime.toString().toLong())?.toFormattedString()
            binding.levelCard.text = "Dummy difficulty"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = TravelCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun getItemCount() = trips.size

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position], viewModel)
    }
}
