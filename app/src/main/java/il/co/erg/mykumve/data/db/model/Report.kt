package il.co.erg.mykumve.data.db.model

import android.graphics.Bitmap
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Report(
    @PropertyName("id") internal var _id: String = "",
    val photo: String? = null,
    val description: String? = "",
    val reporter: String? = "",
    val timestamp: String? = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
){
    val id: String
        get() = _id
}