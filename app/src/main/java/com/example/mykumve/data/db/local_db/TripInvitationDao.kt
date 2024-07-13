package com.example.mykumve.data.db.local_db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.mykumve.data.model.TripInvitation

@Dao
interface TripInvitationDao {
    @Insert
    suspend fun insertTripInvitation(invitation: TripInvitation): Long

    @Update
    suspend fun updateTripInvitation(invitation: TripInvitation)

    @Query("SELECT * FROM trip_invitations WHERE tripId = :tripId")
    fun getTripInvitationsByTripId(tripId: Long): LiveData<List<TripInvitation>>

    @Query("SELECT * FROM trip_invitations WHERE userId = :userId")
    fun getTripInvitationsForUser(userId: Long): LiveData<List<TripInvitation>>

}
