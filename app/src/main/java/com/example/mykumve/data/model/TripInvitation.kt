package com.example.mykumve.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mykumve.util.Converters
import com.example.mykumve.util.TripInvitationStatus

@Entity(tableName = "trip_invitations")
data class TripInvitation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var tripId: Long,
    var userId: Long,
    @TypeConverters(Converters::class) var status: TripInvitationStatus = TripInvitationStatus.PENDING
)
