package il.co.erg.mykumve.data.data_classes

data class Equipment(
    val name: String = "",
    val done: Boolean = false,
    val userId: String? = null,
    val quantity: Int = 1,
)
