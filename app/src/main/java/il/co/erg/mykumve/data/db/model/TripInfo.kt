package il.co.erg.mykumve.data.db.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import il.co.erg.mykumve.data.data_classes.Point
import il.co.erg.mykumve.util.DifficultyLevel
import il.co.erg.mykumve.util.fromDifficultyValue


data class TripInfo(
    @PropertyName("id") internal var _id: String = "",  // Internal mutable field
    var title: String,
    var points: List<Point>? = null,
    var areaId: Int? = null,
    var subAreaId: Int? = null,
    var description: String? = null,
    var routeDescription: String? = null,
    var difficulty: DifficultyLevel = DifficultyLevel.UNSET,
    var length: Float? = null,
    var tags: List<String>? = null,
    var isCircular: Boolean? = null,
    var likes: Int? = null,
    var link: String? = null,
    var publishedDate: String? = null,
    var isImported: Boolean = false,
    val imageInfo: Map<String, String>? = null,
) {
    val id: String
        get() = _id  // Public read-only property

    companion object {
        fun fromFirestoreDocument(document: DocumentSnapshot): TripInfo {
            val id = document.get("id") as? String? ?: ""
            val title = document.get("title") as?  String? ?: ""
            val points = document.get("points") as? List<Point>?
            val areaId = document.get("areaId") as?  Int?
            val subAreaId = document.get("subAreaId") as?  Int?
            val description = document.get("description") as?  String? ?: ""
            val routeDescription = document.get("routeDescription") as?  String? ?: ""
            val difficultyValue = document.get("difficulty") as?  DifficultyLevel? ?: DifficultyLevel.UNSET
            val difficulty = fromDifficultyValue(difficultyValue)
            val length = document.get("length") as? Float? ?: 0.0f
            val tags = document.get("tags") as?  List<String>? ?: emptyList()
            val isCircular = document.get("isCircular") as?  Boolean? ?: false
            val likes = document.get("likes") as?  Int? ?: 0
            val link = document.get("link") as?  String? ?: ""
            val publishedDate = document.get("publishedDate").toString() as?  String? ?: ""
            val isImported = document.get("isImported") as?  Boolean? ?: false
            val imageInfo = document.get("imageInfo") as? Map<String, String>? ?: emptyMap<String, String>()

            return TripInfo(
                _id = id,
                title = title,
                points = points,
                areaId = areaId,
                subAreaId = subAreaId,
                description = description,
                routeDescription = routeDescription,
                difficulty = difficulty,
                length = length,
                tags = tags,
                isCircular = isCircular,
                likes = likes,
                link = link,
                publishedDate = publishedDate,
                isImported = isImported,
                imageInfo = imageInfo,
            )
        }
    }
}
