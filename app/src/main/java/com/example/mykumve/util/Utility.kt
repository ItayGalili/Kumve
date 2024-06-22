package com.example.mykumve.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utility {
    fun Date.toFormattedString(): String {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(this)

    }
}