package il.co.erg.mykumve.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "sub_areas",
    foreignKeys = [ForeignKey(entity = Area::class, parentColumns = ["id"], childColumns = ["area_id"], onDelete = CASCADE)],
            indices = [androidx.room.Index(value = ["area_id"]),]

)
data class SubArea(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "area_id") val areaId: Int,
    @ColumnInfo(name = "name_key") val nameKey: String // Translation key for subarea name
)
