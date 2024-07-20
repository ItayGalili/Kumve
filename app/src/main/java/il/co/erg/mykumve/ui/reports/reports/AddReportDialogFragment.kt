package il.co.erg.mykumve.ui.reports

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.model.Report
import il.co.erg.mykumve.util.ImagePickerUtil
import il.co.erg.mykumve.util.UserManager
import il.co.erg.mykumve.util.UserUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddReportDialogFragment : DialogFragment() {

    // Define interface for communication with parent fragment
    interface OnReportAddedListener {
        fun onReportAdded(report: Report)
    }

    private var listener: OnReportAddedListener? = null
    private lateinit var reportImage: ImageView
    private lateinit var descriptionEditText: EditText
    private lateinit var imagePickerUtil: ImagePickerUtil

    private var capturedImageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_report_dialog, container, false)

        // Initialize views
        reportImage = view.findViewById(R.id.report_image)
        descriptionEditText = view.findViewById(R.id.report_description)

        // Initialize ImagePickerUtil
        imagePickerUtil = ImagePickerUtil(this) { uri ->
            uri?.let {
                reportImage.setImageURI(it)
                // Convert Uri to Bitmap and store it
                capturedImageBitmap = uriToBitmap(it)
            }
        }

        // Capture image button click
        reportImage.setOnClickListener {
            showImagePickerDialog()
        }

        // Save button click
        val saveButton = view.findViewById<Button>(R.id.report_now)
        saveButton.setOnClickListener {
            saveReport()
            dismiss()
        }

        return view
    }

    private fun showImagePickerDialog() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Add Photo!")
        builder.setItems(items) { dialog, item ->
            when {
                items[item] == "Take Photo" -> {
                    imagePickerUtil.requestCaptureImagePermission()
                    dialog.dismiss()
                }
                items[item] == "Choose from Library" -> {
                    imagePickerUtil.pickImage()
                    dialog.dismiss()
                }
                items[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun saveReport() {
        val description = descriptionEditText.text.toString().trim()

        if (description.isNotEmpty() && capturedImageBitmap != null) {
            val timeStamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val reporterName = UserUtils.getFullName(UserManager.getUser())
            // Create a Report object with captured image, description, reporter name, and timestamp
            val report = Report(capturedImageBitmap!!, description, "Reported By: $reporterName", timeStamp)

            // Notify parent fragment (UsersReports) through the listener
            listener?.onReportAdded(report)
        }
    }

    fun setOnReportAddedListener(listener: OnReportAddedListener) {
        this.listener = listener
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}
