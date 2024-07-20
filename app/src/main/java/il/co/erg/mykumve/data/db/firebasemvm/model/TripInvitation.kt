package il.co.erg.mykumve.data.db.firebasemvm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import il.co.erg.mykumve.util.Converters
import il.co.erg.mykumve.util.TripInvitationStatus

@Entity(tableName = "trip_invitations")
data class TripInvitation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var tripId: Long,
    var userId: Long,
    @TypeConverters(Converters::class) var status: TripInvitationStatus = TripInvitationStatus.PENDING
)
