package com.example.mykumve.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @ColumnInfo(name = "first_name") val firstName: String,
    @ColumnInfo(name = "surname") var surname: String?,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "profile_photo_uri") var photo: String?,
    @ColumnInfo(name = "phone") var phone: String?,
    @ColumnInfo(name = "password") val hashedPassword: String,
    @ColumnInfo(name = "salt") val salt: String,
    @PrimaryKey(autoGenerate = true) var id: Long = 0
)
