package com.example.mykumve.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mykumve.util.Converters
import com.example.mykumve.util.DifficultyLevel

@Entity(tableName = "trips",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["trip_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(Converters::class)
data class TripInfo(
    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "place")
    val place: String?,

    @ColumnInfo(name = "difficulty")
    val level: DifficultyLevel,

    @ColumnInfo(name = "photo_uri")
    val photo: String?,

    @ColumnInfo(name = "trip_id")
    var tripId: Int,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
