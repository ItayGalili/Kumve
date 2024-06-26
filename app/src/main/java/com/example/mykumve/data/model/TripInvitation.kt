package com.example.mykumve.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mykumve.util.Converters
import com.example.mykumve.util.TripInvitationStatus

@Entity(tableName = "trip_invitations")
@TypeConverters(Converters::class)
data class TripInvitation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var tripId: Int,
    var userId: Int,
    var status: TripInvitationStatus = TripInvitationStatus.PENDING
)
