package il.co.erg.mykumve.data.db.model

import com.google.firebase.firestore.PropertyName

data class User(
    @PropertyName("id") internal var _id: String = "",
    var firstName: String = "",
    var surname: String? = null,
    var email: String = "",
    var photo: String? = null,
    var phone: String? = null,
    var hashedPassword: String = "",
    val salt: String = ""
) {
    val id: String
        get() = _id // Public read-only property
}
