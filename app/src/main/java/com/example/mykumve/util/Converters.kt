package com.example.mykumve.util

import androidx.room.TypeConverter
import com.example.mykumve.data.model.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

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
    fun fromUserList(users: List<User>?): String? {
        if (users == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.toJson(users, type)
    }

    @TypeConverter
    fun toUserList(usersString: String?): List<User>? {
        if (usersString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(usersString, type)
    }

    @TypeConverter
    fun fromStringList(strings: List<String>?): String? {
        if (strings == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(strings, type)
    }

    @TypeConverter
    fun toStringList(stringsString: String?): List<String>? {
        if (stringsString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringsString, type)
    }

    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun fromEquipmentList(equipments: List<Equipment>?): String? {
        if (equipments == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Equipment>>() {}.type
        return gson.toJson(equipments, type)
    }

    @TypeConverter
    fun toEquipmentList(equipmentsString: String?): List<Equipment>? {
        if (equipmentsString == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<Equipment>>() {}.type
        return gson.fromJson(equipmentsString, type)
    }

    @TypeConverter
    fun fromTripInvitationStatus(value: Int): TripInvitationStatus {
        return TripInvitationStatus.fromInt(value)
    }

    @TypeConverter
    fun tripInvitationStatusToInt(status: TripInvitationStatus): Int {
        return status.value
    }


}
