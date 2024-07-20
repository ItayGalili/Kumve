package il.co.erg.mykumve.data.db.local_db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import il.co.erg.mykumve.data.data_classes.Point
import il.co.erg.mykumve.util.Converters
import il.co.erg.mykumve.util.DifficultyLevel

@Entity(tableName = "trip_info")
@TypeConverters(Converters::class)
data class TripInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "points") var points: List<Point>?,
    @ColumnInfo(name = "area_id") var areaId: Int?,
    @ColumnInfo(name = "sub_area_id") var subAreaId: Int?,
    @ColumnInfo(name = "description") var description: String?,
    @ColumnInfo(name = "route_description") var routeDescription: String?,
    @ColumnInfo(name = "difficulty") var difficulty: DifficultyLevel?,
    @ColumnInfo(name = "length") var length: Float?,
    @ColumnInfo(name = "tags") var tags: List<String>?,
    @ColumnInfo(name = "is_circular") var isCircular: Boolean?,
    @ColumnInfo(name = "likes") var likes: Int?,
    @ColumnInfo(name = "trip_id") var tripId: Long
)