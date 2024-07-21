package il.co.erg.mykumve.data.db.model

import com.google.firebase.firestore.PropertyName

data class Area(
    @PropertyName("id") internal var _id: String = "",
    val nameKey: String // Translation key for area name
) {
    val id: String
        get() = _id
}
