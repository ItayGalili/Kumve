package com.example.mykumve.ui.trip.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.ItemPartnerCardBinding

class PartnerListAdapter : RecyclerView.Adapter<PartnerListAdapter.PartnerViewHolder>() {

    private val userList = mutableListOf<User>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
        val binding = ItemPartnerCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    fun addUser(user: User) {
        userList.add(user)
        notifyItemInserted(userList.size - 1)
    }

    fun removeUserAtPosition(position: Int) {
        userList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getUserAtPosition(position: Int): User = userList[position]

    fun getAllUsers(): List<User> = userList

    inner class PartnerViewHolder(private val binding: ItemPartnerCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.textViewPartnerName.text = user.firstName
            Glide.with(binding.imageViewPartner.context)
                .load(user.photo) // Assuming `photo` is the URL or path to the image
                .into(binding.imageViewPartner)

            // Set up buttons
            binding.ComeBtn.setOnClickListener {
                // Handle "I'm coming" button click
            }

            binding.NotComBtm.setOnClickListener {
                // Handle "I'm not coming" button click
            }
        }
    }
}
