package com.example.mykumve.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mykumve.util.DifficultyLevel

@Entity(tableName = "trip_info")
data class TripInfo(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "start_point") val startPoint: String,
    @ColumnInfo(name = "end_point") val endPoint: String,
    @ColumnInfo(name = "route_description") val routeDescription: String,
    @ColumnInfo(name = "start_point_description") val startPointDescription: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "difficulty") val difficulty: DifficultyLevel,
    @ColumnInfo(name = "area") val area: String,
    @ColumnInfo(name = "length") val length: Double,
    @ColumnInfo(name = "tags") val tags: List<String>?,
    @ColumnInfo(name = "general_notes") val generalNotes: String?,
)
