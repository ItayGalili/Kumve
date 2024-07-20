package il.co.erg.mykumve.util
//
//import androidx.room.TypeConverter
//import il.co.erg.mykumve.data.data_classes.Equipment
//import il.co.erg.mykumve.data.db.local_db.model.Area
//import il.co.erg.mykumve.data.data_classes.Point
//import il.co.erg.mykumve.data.db.local_db.model.TripInvitation
//import il.co.erg.mykumve.data.db.firebasemvm.model.User
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import java.util.Date
//
//class Converters {
//
//    @TypeConverter
//    fun fromDifficultyLevel(difficultyLevel: DifficultyLevel): Int {
//        return difficultyLevel.id
//    }
//
//    @TypeConverter
//    fun toDifficultyLevel(id: Int): DifficultyLevel {
//        return DifficultyLevel.fromId(id)
//    }
//
//    @TypeConverter
//    fun fromUserList(users: List<User>?): String? {
//        if (users == null) {
//            return null
//        }
//        val gson = Gson()
//        val type = object : TypeToken<List<User>>() {}.type
//        return gson.toJson(users, type)
//    }
//
//    @TypeConverter
//    fun toUserList(usersString: String?): List<User>? {
//        if (usersString == null) {
//            return null
//        }
//        val gson = Gson()
//        val type = object : TypeToken<List<User>>() {}.type
//        return gson.fromJson(usersString, type)
//    }
//
//    @TypeConverter
//    fun fromStringList(strings: List<String>?): String? {
//        if (strings == null) {
//            return null
//        }
//        val gson = Gson()
//        val type = object : TypeToken<List<String>>() {}.type
//        return gson.toJson(strings, type)
//    }
//
//    @TypeConverter
//    fun toStringList(stringsString: String?): List<String>? {
//        if (stringsString == null) {
//            return null
//        }
//        val gson = Gson()
//        val type = object : TypeToken<List<String>>() {}.type
//        return gson.fromJson(stringsString, type)
//    }
//
//    @TypeConverter
//    fun toDate(timestamp: Long?): Date? {
//        return timestamp?.let { Date(it) }
//    }
//
//    @TypeConverter
//    fun fromEquipmentList(equipments: List<Equipment>?): String? {
//        if (equipments == null) {
//            return null
//        }
//        val gson = Gson()
//        val type = object : TypeToken<List<Equipment>>() {}.type
//        return gson.toJson(equipments, type)
//    }
//
//    @TypeConverter
//    fun toEquipmentList(equipmentsString: String?): List<Equipment>? {
//        if (equipmentsString == null) {
//            return null
//        }
//        val gson = Gson()
//        val type = object : TypeToken<List<Equipment>>() {}.type
//        return gson.fromJson(equipmentsString, type)
//    }
//
//    @TypeConverter
//    fun fromTripInvitationList(value: List<TripInvitation>?): String {
//        val type = object : TypeToken<List<TripInvitation>>() {}.type
//        return Gson().toJson(value, type)
//    }
//
//    @TypeConverter
//    fun toTripInvitationList(value: String): List<TripInvitation>? {
//        val type = object : TypeToken<List<TripInvitation>>() {}.type
//        return Gson().fromJson(value, type)
//    }
//
//    @TypeConverter
//    fun fromShareLevel(value: Int): ShareLevel {
//        return ShareLevel.fromInt(value)
//    }
//
//    @TypeConverter
//    fun shareLevelToInt(level: ShareLevel): Int {
//        return level.value
//    }
//
//    @TypeConverter
//    fun fromPointList(value: List<Point>?): String {
//        val type = object : TypeToken<List<Point>>() {}.type
//        return Gson().toJson(value, type)
//    }
//
//    @TypeConverter
//    fun toPointList(value: String): List<Point>? {
//        val type = object : TypeToken<List<Point>>() {}.type
//        return Gson().fromJson(value, type)
//    }
//
//    @TypeConverter
//    fun fromAreaList(value: List<Area>?): String {
//        val type = object : TypeToken<List<Area>>() {}.type
//        return Gson().toJson(value, type)
//    }
//
//    @TypeConverter
//    fun toAreaList(value: String): List<Area>? {
//        val type = object : TypeToken<List<Area>>() {}.type
//        return Gson().fromJson(value, type)
//    }
//
//}
