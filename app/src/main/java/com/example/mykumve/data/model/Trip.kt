package com.example.mykumve.data.model

import com.example.mykumve.util.Converters
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "trips",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
        entity = TripInfo::class,
        parentColumns = ["id"],
        childColumns = ["trip_info_id"],
        onDelete = ForeignKey.SET_NULL
    )]
)
@TypeConverters(Converters::class)
data class Trip(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "gather_time") val gatherTime: Long?,
    @ColumnInfo(name = "gather_place") val gatherPlace: String?,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "participants") val participants: List<User>?,
    @ColumnInfo(name = "equipment") val equipment: List<String>?,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "trip_info_id") val tripInfoId: Int? = null

)
