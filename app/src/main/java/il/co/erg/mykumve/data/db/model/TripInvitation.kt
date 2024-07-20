package il.co.erg.mykumve.data.db.model

import il.co.erg.mykumve.util.TripInvitationStatus

data class TripInvitation(
    internal var _id: String = "",
    var tripId: String,
    var userId: String,
    var status: TripInvitationStatus = TripInvitationStatus.PENDING
) {
    val id: String
        get() = _id  // Public read-only property
}
