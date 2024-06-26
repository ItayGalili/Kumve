package com.example.mykumve.data.model

import com.example.mykumve.util.Converters
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.mykumve.util.Equipment
import com.example.mykumve.util.ShareLevel

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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "gather_time") var gatherTime: Long?,
    @ColumnInfo(name = "participants") var participants: MutableList<User>?,
    @ColumnInfo(name = "image") var image: String?,
    @ColumnInfo(name = "equipment") var equipment: List<String>?,
    @ColumnInfo(name = "user_id") var userId: Int,
    @ColumnInfo(name = "trip_info_id") var tripId: Int,
    @ColumnInfo(name = "notes") var notes: List<String>?,
    @ColumnInfo(name = "end_date") var endDate: Long?,
    @ColumnInfo(name = "invitations") var invitations: MutableList<TripInvitation> = mutableListOf(),
    @ColumnInfo(name = "share_level") var shareLevel: ShareLevel = ShareLevel.PUBLIC
)
