package com.example.mykumve

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.databinding.TravelCardBinding


class ItemAdapter(val items:List<Item>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
        class ItemViewHolder(private val binding: TravelCardBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: Item){
            binding.tripTitle.text = item.title
            binding.areaCard.text = item.place
            binding.dateCard.text = item.date
            binding.levelCard.text = item.level
            //TODO: load the image
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(TravelCardBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount() = items.size



    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(items[position])
}
