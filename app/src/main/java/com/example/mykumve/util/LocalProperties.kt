package com.example.mykumve.util

import com.example.mykumve.ui.map.MapFragment
import java.util.Properties
import android.content.Context

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