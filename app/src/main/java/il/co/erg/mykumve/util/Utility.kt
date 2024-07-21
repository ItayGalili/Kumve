package il.co.erg.mykumve.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.User

object Utility {
    fun Date.toFormattedString(): String {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return format.format(this)
    }

    fun timestampToString(timestamp: Long?): String? {
        return timestamp?.let { Date(it).toFormattedString() }
    }
}

object UserUtils {
    fun getFullName(user: User?): String {
        if (user != null) {
            return "${user.firstName} ${user.surname ?: ""}".trim()
        }
        return ""
    }

    fun normalizePhoneNumber(phoneNumber: String, defaultCountryCode: String = "+972"): String {
        // Remove all spaces and other non-digit characters
        val cleanedPhoneNumber = phoneNumber.replace("\\D".toRegex(), "")

        // Ensure the number matches the valid phone pattern
        if (!isValidPhoneNumber(cleanedPhoneNumber)) {
            throw IllegalArgumentException("Invalid format: Phone number must start with '0' followed by 9 digits.")
        }

        // Remove the leading zero and add the default country code
        val normalizedPhoneNumber = defaultCountryCode + cleanedPhoneNumber.substring(1)

        return normalizedPhoneNumber
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        // Regex to match an international phone number with a country code (+1 to +9999) optionally followed by a space,
        // then either 10 digits starting with 0 or 9 digits not starting with 0.
        val phoneRegex = Regex(PATTERNS.VALID_PHONE)
        return phoneRegex.matches(phone)
    }
}

object TripInfoUtils {
    fun mapDifficultyToModel(context: Context, difficulty: String): DifficultyLevel {
        return when (difficulty) {
            context.getString(R.string.difficulty_easy) -> DifficultyLevel.EASY
            context.getString(R.string.difficulty_medium) -> DifficultyLevel.MEDIUM
            context.getString(R.string.difficulty_hard) -> DifficultyLevel.HARD
            else -> DifficultyLevel.UNSET
        }
    }

    fun mapDifficultyToString(context: Context, difficulty: DifficultyLevel?): String {
        return when (difficulty) {
            DifficultyLevel.EASY -> context.getString(R.string.difficulty_easy)
            DifficultyLevel.MEDIUM -> context.getString(R.string.difficulty_medium)
            DifficultyLevel.HARD -> context.getString(R.string.difficulty_hard)
            DifficultyLevel.UNSET, null -> context.getString(R.string.difficulty_unset)
        }
    }

    fun mapAreaToModel(context: Context, area: String): Int? {
        return when (area) {
            context.getString(R.string.upper_galilee) -> 2
            context.getString(R.string.western_galilee) -> 3
            context.getString(R.string.haifa_and_carmel) -> 4
            context.getString(R.string.lower_galilee) -> 5
            context.getString(R.string.sharon) -> 7
            context.getString(R.string.shfela) -> 8
            context.getString(R.string.samaria) -> 9
            context.getString(R.string.coastal_plain_and_gush_dan) -> 10
            context.getString(R.string.jerusalem_hills_and_beit_shemesh) -> 12
            context.getString(R.string.jerusalem) -> 13
            context.getString(R.string.dead_sea_and_judean_desert) -> 15
            context.getString(R.string.northern_negev) -> 16
            context.getString(R.string.central_negev_and_craters) -> 17
            context.getString(R.string.southern_negev_and_eilat_mountains) -> 18
            else -> null
        }
    }

    fun mapAreaToString(context: Context, areaId: Int?): String {
        return when (areaId) {
//        1 -> context.getString(R.string.north)
            2 -> context.getString(R.string.upper_galilee)
            3 -> context.getString(R.string.western_galilee)
            4 -> context.getString(R.string.haifa_and_carmel)
            5 -> context.getString(R.string.lower_galilee)
//        6 -> context.getString(R.string.center)
            7 -> context.getString(R.string.sharon)
            8 -> context.getString(R.string.shfela)
            9 -> context.getString(R.string.samaria)
            10 -> context.getString(R.string.coastal_plain_and_gush_dan)
//        11 -> context.getString(R.string.jerusalem_and_surroundings)
            12 -> context.getString(R.string.jerusalem_hills_and_beit_shemesh)
            13 -> context.getString(R.string.jerusalem)
//        14 -> context.getString(R.string.south)
            15 -> context.getString(R.string.dead_sea_and_judean_desert)
            16 -> context.getString(R.string.northern_negev)
            17 -> context.getString(R.string.central_negev_and_craters)
            18 -> context.getString(R.string.southern_negev_and_eilat_mountains)
            else -> context.getString(R.string.unknown_area)
        }
    }
}


