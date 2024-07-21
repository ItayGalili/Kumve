package il.co.erg.mykumve.data.db.model

import com.google.firebase.firestore.PropertyName

data class SubArea(
    @PropertyName("id") internal var _id: String = "",
    val areaId: String,
    val nameKey: String
) {
    val id: String
        get() = _id
}
