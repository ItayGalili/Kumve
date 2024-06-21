package com.example.mykumve.util

import androidx.room.TypeConverter
import com.example.mykumve.data.model.Trip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class Converters {

    @TypeConverter
    fun fromDifficultyLevel(difficultyLevel: DifficultyLevel): Int {
        return difficultyLevel.id
    }

    @TypeConverter
    fun toDifficultyLevel(id: Int): DifficultyLevel {
        return DifficultyLevel.fromId(id)
    }

    @TypeConverter
    fun fromTripList(trips: List<Trip>?): String? {
        if (trips == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Trip>>() {}.type
        return gson.toJson(trips, type)
    }

    @TypeConverter
    fun toTripList(tripsString: String?): List<Trip>? {
        if (tripsString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Trip>>() {}.type
        return gson.fromJson(tripsString, type)
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}

fun Long.toFormattedDateString(): String {
    val date = Date(this)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}

fun String.toTimestamp(): Long {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = format.parse(this)
    return date?.time ?: 0
}
