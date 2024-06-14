package com.example.mykumve

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter {}
/*
class ItemAdapter(val items:List<Item>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
        class ItemViewHolder(private val binding: ItemLayuotBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: Item){
            binding.itemTitle.text = item.title
            binding.itemDescription.text = item.description
            //TODO: load the image
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(ItemLayuotBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount() = items.size



    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(items[position])
}*/
