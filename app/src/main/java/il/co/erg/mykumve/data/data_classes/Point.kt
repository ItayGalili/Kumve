package il.co.erg.mykumve.data.data_classes

data class Point(
    val latitude: Float,
    val longitude: Float
) {
    companion object {
        fun fromString(latLng: String): Point {
            val parts = latLng.split(",")
            return Point(parts[0].toFloat(), parts[1].toFloat())
        }

        fun toString(point: Point): String {
            return "${point.latitude},${point.longitude}"
        }
    }
}
