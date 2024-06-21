package com.example.mykumve.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mykumve.util.Converters

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @ColumnInfo(name = "first_name")
    val firstName: String,

    @ColumnInfo(name = "surname")
    val surname: String,

    @ColumnInfo(name = "profile_photo_uri")
    val photo: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "trips")
    val trips: List<Trip>?,

    @ColumnInfo(name = "password")
    val hashedPassword: String,

    @ColumnInfo(name = "salt")
    val salt: String,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
