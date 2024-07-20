package il.co.erg.mykumve.data.db.model

import il.co.erg.mykumve.data.data_classes.Point
import il.co.erg.mykumve.util.DifficultyLevel


data class TripInfo(
    internal var _id: String = "",  // Internal mutable field
    var title: String,
    var points: List<Point>? = null,
    var areaId: Int? = null,
    var subAreaId: Int? = null,
    var description: String? = null,
    var routeDescription: String? = null,
    var difficulty: DifficultyLevel? = null,
    var length: Float? = null,
    var tags: List<String>? = null,
    var isCircular: Boolean? = null,
    var likes: Int? = null,
    var tripId: String  // Reference to Trip document ID
) {
    val id: String
        get() = _id  // Public read-only property
}