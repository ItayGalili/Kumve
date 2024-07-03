package com.example.mykumve.ui.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.databinding.ItemPartnerCardBinding
import com.example.mykumve.ui.viewmodel.UserViewModel

class PartnerListAdapter(
    private val userViewModel: UserViewModel
) : ListAdapter<TripInvitation, PartnerListAdapter.PartnerViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
        val binding =
            ItemPartnerCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
        val invitation = getItem(position)
        holder.bind(invitation)
    }

    inner class PartnerViewHolder(private val binding: ItemPartnerCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(invitation: TripInvitation) {
            val invitedUser = userViewModel.getUserById(invitation.userId)?.value
            val userFullName = "${invitedUser?.firstName} ${invitedUser?.surname}"
            binding.textViewPartnerName.text = userFullName
            Glide.with(binding.imageViewPartner.context)
                .load(invitedUser?.photo) // Assuming `photo` is the URL or path to the image
                .into(binding.imageViewPartner)

            // Set up buttons
            binding.ComeBtn.setOnClickListener {
                // Handle "I'm coming" button click
            }
        }
    }

    fun getInvitationAtPosition(position: Int): TripInvitation = getItem(position)

    fun removeInvitationAtPosition(position: Int) {
        val currentList = currentList.toMutableList()
        currentList.removeAt(position)
        submitList(currentList)
    }

    class DiffCallback : DiffUtil.ItemCallback<TripInvitation>() {
        override fun areItemsTheSame(oldItem: TripInvitation, newItem: TripInvitation): Boolean {
            return oldItem.id == newItem.id // Assuming `id` is a unique identifier in `TripInvitation`
        }

        override fun areContentsTheSame(oldItem: TripInvitation, newItem: TripInvitation): Boolean {
            return oldItem == newItem
        }
    }
}
