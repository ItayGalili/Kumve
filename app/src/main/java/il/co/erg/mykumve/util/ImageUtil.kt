package il.co.erg.mykumve.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog

class ImagePickerUtil(
    private val fragment: Fragment,
    private val onImagePicked: (Uri) -> Unit,
    private val onImageUploadResult: (Boolean, String?) -> Unit
) {
    private var imageUri: Uri? = null
    var downloadUrl: String? = null
        private set

    val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let { uri ->
                fragment.requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = uri
                onImagePicked(uri)
                uploadImageToFirestore(uri)
            }
        }

    val captureImageLauncher: ActivityResultLauncher<Uri> =
        fragment.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let { uri ->
                    onImagePicked(uri)
                    uploadImageToFirestore(uri)
                }
            }
        }

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                captureImage()
            } else {
                // Handle the case where the user denied the permission
            }
        }

    fun pickImage() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    fun captureImage() {
        imageUri = createImageUri()
        imageUri?.let { uri ->
            captureImageLauncher.launch(uri)
        }
    }

    fun requestCaptureImagePermission() {
        when {
            ContextCompat.checkSelfPermission(fragment.requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                captureImage()
            }
            fragment.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                // Show an explanation to the user why the permission is needed
                // Then request the permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun createImageUri(): Uri? {
        val contentResolver = fragment.requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "new_image.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun uploadImageToFirestore(uri: Uri) {
        val storageRef = Firebase.storage.reference.child("images/${uri.lastPathSegment}")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                downloadUrl = downloadUri.toString()
                onImageUploadResult(true, downloadUrl)
            }
        }.addOnFailureListener {
            onImageUploadResult(false, null)
        }
    }

    fun showImagePickerDialog(context: Context) {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update Profile Picture")
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> {
                    requestCaptureImagePermission()
                    dialog.dismiss()
                }
                items[item] == "Choose from Library" -> {
                    pickImage()
                    dialog.dismiss()
                }
                items[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    companion object {
        fun uploadImageToFirestore(uri: Uri, onImageUploadResult: (Boolean, String?) -> Unit) {
            val storageRef = Firebase.storage.reference.child("images/${uri.lastPathSegment}")
            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                    val downloadUrl = downloadUri.toString()
                    onImageUploadResult(true, downloadUrl)
                }
            }.addOnFailureListener {
                onImageUploadResult(false, null)
            }
        }
    }
}

fun loadImage(url: String, imageView: ImageView) {
    Glide.with(imageView.context)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(imageView)
}
