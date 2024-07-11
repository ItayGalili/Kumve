package com.example.mykumve.util

import com.example.mykumve.data.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utility {
    fun Date.toFormattedString(): String {
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault())
        return format.format(this)
    }
    fun timestampToString(timestamp: Long?): String? {
        return timestamp?.let { Date(it).toFormattedString() }
    }
}

object UserUtils {
    fun getFullName(user: User?): String {
        if (user != null){
            return "${user.firstName} ${user.surname ?: ""}".trim()
        }
        return ""
    }
}
