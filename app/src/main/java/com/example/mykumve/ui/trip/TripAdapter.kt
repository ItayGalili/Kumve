package com.example.mykumve.ui.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.data.model.Trip
import com.example.mykumve.databinding.TravelCardBinding


class TripAdapter(val trips:List<Trip>): RecyclerView.Adapter<TripAdapter.TripViewHolder>() {
        class TripViewHolder(private val binding: TravelCardBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(trip: Trip){
            binding.tripTitle.text = trip.title
            binding.areaCard.text = trip.place
            binding.dateCard.text = trip.date
            binding.levelCard.text = trip.level.prettyString().toString()
            //TODO: load the image
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        TripViewHolder(TravelCardBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount() = trips.size



    override fun onBindViewHolder(holder: TripViewHolder, position: Int) =
        holder.bind(trips[position])
}
