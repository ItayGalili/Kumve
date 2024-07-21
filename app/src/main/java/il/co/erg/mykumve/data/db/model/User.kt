package il.co.erg.mykumve.data.db.model

import com.google.firebase.auth.FirebaseUser
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

    // Factory method to create a User from a FirebaseUser
    companion object {
        fun fromFirebaseUser(firebaseUser: FirebaseUser): User {
            return User(
                _id = firebaseUser.uid,
                firstName = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                photo = firebaseUser.photoUrl?.toString(),
                phone = firebaseUser.phoneNumber
            )
        }
    }
}
