package il.co.erg.mykumve.data.db.model

import il.co.erg.mykumve.data.data_classes.Equipment
import il.co.erg.mykumve.util.ShareLevel

data class Trip(
    internal var _id: String = "",
    var title: String = "",
    var description: String? = null,
    var gatherTime: Long? = null,
    var participants: MutableList<User>? = null,
    var image: String? = null,
    var equipment: MutableList<Equipment>? = null,
    var userId: String = "", 
    var tripInfoId: String? = null, 
    var notes: MutableList<String>? = null,
    var endDate: Long? = null,
    var invitations: MutableList<TripInvitation> = mutableListOf(),
    var shareLevel: ShareLevel = ShareLevel.PUBLIC
) {
    val id: String
        get() = _id // Public read-only property
}
