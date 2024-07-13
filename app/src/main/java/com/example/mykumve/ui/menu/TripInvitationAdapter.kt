package com.example.mykumve.ui.notifications

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.data.model.TripInvitation
import com.example.mykumve.databinding.ItemNotificationBinding
import com.example.mykumve.ui.viewmodel.TripViewModel
import com.example.mykumve.util.TripInvitationStatus

class TripInvitationAdapter(
    private var invitations: List<TripInvitation>,
    var tripViewModel: TripViewModel,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<TripInvitationAdapter.TripInvitationViewHolder>() {

    val TAG = TripInvitationAdapter::class.java.simpleName

    inner class TripInvitationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(invitation: TripInvitation) {
            tripViewModel.getTripById(invitation.id)?.observe(lifecycleOwner, Observer { trip ->
                if (trip != null) {
                    binding.invitationTitle.text = trip.title
                    binding.invitationStatus.text = "Status: ${invitation.status}"

                    if (invitation.status in setOf(
                            TripInvitationStatus.APPROVED,
                            TripInvitationStatus.REJECTED
                        )
                    ) {
                        binding.acceptButton.isEnabled = false
                        binding.rejectButton.isEnabled = false
                    } else {
                        binding.acceptButton.setOnClickListener {
                            handleAccept(invitation)
                        }
                        binding.rejectButton.setOnClickListener {
                            handleReject(invitation)
                        }
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripInvitationViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripInvitationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripInvitationViewHolder, position: Int) {
        holder.bind(invitations[position])
    }

    override fun getItemCount(): Int {
        return invitations.size
    }

    fun updateInvitations(newInvitations: List<TripInvitation>) {
        invitations = newInvitations
        notifyDataSetChanged()
    }

    private fun handleAccept(invitation: TripInvitation) {
        invitation.status = TripInvitationStatus.APPROVED
        respondToTripInvitation(invitation)
    }


    private fun handleReject(invitation: TripInvitation) {
        invitation.status = TripInvitationStatus.REJECTED
        respondToTripInvitation(invitation)
    }

    private fun respondToTripInvitation(invitation: TripInvitation) {
        tripViewModel.respondToTripInvitation(invitation) { result ->
            Log.d(TAG, if (result) "TripInvitation Accepted" else "TripInvitation Rejected")
        }
    }

}
