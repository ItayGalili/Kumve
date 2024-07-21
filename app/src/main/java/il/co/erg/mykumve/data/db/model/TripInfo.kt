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
    var difficulty: DifficultyLevel=DifficultyLevel.UNSET,
    var length: Float? = null,
    var tags: List<String>? = null,
    var isCircular: Boolean? = null,
    var likes: Int? = null,
    var link:  String? = null,
    var publishedDate: String? = null,
    var isImported: Boolean = false,
    val imageInfo: Map<String, String>?=null,
) {
    val id: String
        get() = _id  // Public read-only property

    companion object {
        fun fromFirestoreDocument(document: DocumentSnapshot): TripInfo? {
            val name = document.get("title")as? String? ?: ""
            val date = document.get("date").toString()
            val description = document.get("description") as? String? ?: ""  // Safe cast
            val difficultyValue = document.get("difficulty")
            val difficulty = fromDifficultyValue(difficultyValue)
            val length=document.get("length").toString()


            return TripInfo(
                title = name,
                publishedDate = date,
                description = description,
                difficulty = difficulty,
                length=length.toFloat(),
            )
        }
    }
}


