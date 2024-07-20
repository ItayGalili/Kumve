package il.co.erg.mykumve.data.db.firebasemvm.model

data class User(
    internal var _id: String = "",
    var firstName: String = "",
    var surname: String? = null,
    var email: String = "",
    var photo: String? = null,
    var phone: String? = null,
    var hashedPassword: String = "",
    val salt: String = "",
){
    val id: String
        get() = _id // Public read-only property

}

