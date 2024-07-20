package il.co.erg.mykumve.data.db.firebasemvm.model

import android.graphics.Bitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Report(
    val imageBitmap: Bitmap,
    val description: String,
    val reporter: String,
    val timestamp: String = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
)
