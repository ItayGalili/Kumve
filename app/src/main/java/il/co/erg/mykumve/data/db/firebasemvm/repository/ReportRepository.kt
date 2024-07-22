package il.co.erg.mykumve.data.db.firebasemvm.repository

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.firebasemvm.util.safeCall
import il.co.erg.mykumve.data.db.model.Report
import il.co.erg.mykumve.util.ImagePickerUtil
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReportRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val reportsCollection = db.collection("reports")

    suspend fun addReport(report: Report): Resource<String> {
        return safeCall {
            val reportId = reportsCollection.document().id
            val imageUri = report.photo?.toUri()
            if (imageUri != null) {
                val uploadImageResult = suspendCoroutine<Resource<String>> { continuation ->
                    ImagePickerUtil.uploadImageToFirestore(imageUri) { success, downloadUrl ->
                        if (success && downloadUrl != null) {
                            continuation.resume(Resource.success(downloadUrl))
                        } else {
                            continuation.resume(Resource.error("Failed to upload image"))
                        }
                    }
                }

                if (uploadImageResult.status == Status.SUCCESS) {
                    val newReport = report.copy(photo = uploadImageResult.data)
                    reportsCollection.document(reportId).set(newReport).await()
                    Resource.success("Report added successfully")
                } else {
                    uploadImageResult
                }
            } else {
                reportsCollection.document(reportId).set(report).await()
                Resource.success("Report added successfully without image")
            }
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
