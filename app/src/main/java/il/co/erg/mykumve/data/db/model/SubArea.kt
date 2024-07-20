package il.co.erg.mykumve.data.db.model

data class SubArea(
    internal var _id: String = "",
    val areaId: String,
    val nameKey: String
) {
    val id: String
        get() = _id
}
