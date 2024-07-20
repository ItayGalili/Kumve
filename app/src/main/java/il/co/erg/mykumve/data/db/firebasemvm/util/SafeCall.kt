package il.co.erg.mykumve.data.db.firebasemvm.util

inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> {
    return try {
        action()
    } catch (e: Exception) {
        Resource.Error(message = e.message ?: "An unknown error occurred")
    }
}
