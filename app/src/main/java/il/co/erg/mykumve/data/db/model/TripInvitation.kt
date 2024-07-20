package il.co.erg.mykumve.data.db.model

import com.google.firebase.firestore.PropertyName
import il.co.erg.mykumve.util.TripInvitationStatus

data class TripInvitation(
    @PropertyName("id") internal var _id: String = "",
    var tripId: String = "",
    var userId: String = "",
    var status: TripInvitationStatus = TripInvitationStatus.PENDING
) {
    val id: String
        get() = _id  // Public read-only property
}
