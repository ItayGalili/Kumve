package com.example.mykumve.util

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class ImagePickerUtil(private val fragment: Fragment, private val onImagePicked: (Uri) -> Unit) {

    private var imageUri: Uri? = null

    val pickImageLauncher: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let { uri ->
                fragment.requireActivity().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = uri
                onImagePicked(uri)
            }
        }

    fun pickImage() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    fun getImageUri(): Uri? {
        return imageUri
    }
}
