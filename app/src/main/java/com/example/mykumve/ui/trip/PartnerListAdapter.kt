package com.example.mykumve.ui.trip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mykumve.data.model.User
import com.example.mykumve.databinding.ItemPartnerCardBinding
import com.example.mykumve.ui.viewmodel.UserViewModel
import com.example.mykumve.util.UserUtils

class PartnerListAdapter(
    private val userViewModel: UserViewModel,
    private val lifecycleOwner: LifecycleOwner

) : ListAdapter<User, PartnerListAdapter.PartnerViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartnerViewHolder {
        val binding =
            ItemPartnerCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartnerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartnerViewHolder, position: Int) {
        val participant = getItem(position)
        holder.bind(participant)
    }

    inner class PartnerViewHolder(private val binding: ItemPartnerCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            val userFullName = UserUtils.getFullName(user)
            binding.textViewPartnerName.text = userFullName
            Glide.with(binding.imageViewPartner.context)
                .load(user.photo) // Assuming `photo` is the URL or path to the image
                .into(binding.imageViewPartner)
        }
    }

    fun getParticipantAtPosition(position: Int): User = getItem(position)

    fun removeParticipantAtPosition(position: Int) {
        val currentList = currentList.toMutableList()
        currentList.removeAt(position)
        submitList(currentList)

    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
