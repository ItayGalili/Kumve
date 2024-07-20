package il.co.erg.mykumve.data.db.firebasemvm.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.model.Report
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ReportRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val reportsCollection = db.collection("reports")

    suspend fun addReport(report: Report): Resource<String> {
        return try {
            val reportId = reportsCollection.document().id
            val imagePath = "reports_images/$reportId.jpg"
            val uploadImageResult = report.imageBitmap?.let { uploadImage(it, imagePath) }

            if (uploadImageResult != null && uploadImageResult.status == Status.SUCCESS) {
                val newReport = report.copy(imageBitmap = null)
                reportsCollection.document(reportId).set(newReport).await()
                Resource.success("Report added successfully")
            } else {
                Resource.error("Failed to upload image")
            }
        } catch (e: Exception) {
            Log.e("ReportRepository", "Error adding report", e)
            Resource.error("Failed to add report: ${e.message}")
        }
    }

    suspend fun getReports(): Resource<List<Report>> {
        return try {
            val snapshot = reportsCollection.get().await()
            val reports = snapshot.documents.mapNotNull { it.toObject<Report>() }
            Resource.success(reports)
        } catch (e: Exception) {
            Log.e("ReportRepository", "Error fetching reports", e)
            Resource.error("Failed to fetch reports: ${e.message}")
        }
    }

    private suspend fun uploadImage(image: Bitmap, path: String): Resource<String> {
        return try {
            val storageRef = storage.reference.child(path)
            val baos = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }
            storageRef.putBytes(data, metadata).await()
            Resource.success("Image uploaded successfully")
        } catch (e: Exception) {
            Log.e("ReportRepository", "Error uploading image", e)
            Resource.error("Failed to upload image: ${e.message}")
        }
    }
}
