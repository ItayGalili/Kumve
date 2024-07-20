package il.co.erg.mykumve.data.db.local_db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "areas")
data class Area(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nameKey: String // Translation key for area name
)
