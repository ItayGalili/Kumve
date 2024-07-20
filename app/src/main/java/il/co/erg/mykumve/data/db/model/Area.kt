package il.co.erg.mykumve.data.db.model

data class Area(
    internal var _id: String = "",
    val nameKey: String // Translation key for area name
) {
    val id: String
        get() = _id
}
