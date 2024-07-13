package com.example.mykumve.ui.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.databinding.ItemInvitationCardBinding
import com.example.mykumve.databinding.ItemPartnerCardBinding
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.UserUtils

class InvitationListAdapter(
    private val userViewModel: UserViewModel,
    private val lifecycleOwner: LifecycleOwner

) : ListAdapter<TripInvitation, InvitationListAdapter.InvitationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val binding =
            ItemInvitationCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InvitationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val invitation = getItem(position)
        holder.bind(invitation)
    }

    inner class InvitationViewHolder(private val binding: ItemInvitationCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(invitation: TripInvitation) {
            userViewModel.getUserById(invitation.userId)?.observe(lifecycleOwner, Observer { invitedUser ->
                val userFullName = UserUtils.getFullName(invitedUser)
                binding.textViewInvitedName.text = userFullName
                binding.invitationStatus.text = invitation.status.toString()
                Glide.with(binding.imageViewInvited.context)
                    .load(invitedUser?.photo) // Assuming `photo` is the URL or path to the image
                    .into(binding.imageViewInvited)
            })
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
