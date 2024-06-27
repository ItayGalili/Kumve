package com.example.mykumve.util

import android.content.Context
import java.util.Properties

object LocalProperties {
    fun getApiKey(context: Context): String {
        val properties = Properties()
        try {
            context.assets.open("local.properties").use { inputStream ->
                properties.load(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return properties.getProperty("apiKey", "")
    }
}