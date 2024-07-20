package il.co.erg.mykumve.data.db.firebasemvm.util

import android.util.Log

inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> {
    return try {
        action()
    } catch (e: Exception) {
        Log.e("SafeCall", "Failed calling FireBase.\n${e.message}")
        Resource.error(message = e.message ?: "An unknown error occurred")
    }
}
