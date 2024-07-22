package il.co.erg.mykumve.ui.reports

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import il.co.erg.mykumve.data.db.model.Report
import il.co.erg.mykumve.databinding.AddReportDialogBinding
import il.co.erg.mykumve.ui.viewmodel.ReportsViewModel
import il.co.erg.mykumve.util.ImagePickerUtil
import il.co.erg.mykumve.util.UserManager
import il.co.erg.mykumve.util.UserUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddReportDialogFragment : DialogFragment() {
    private val TAG = AddReportDialogFragment::class.java.simpleName

    interface OnReportAddedListener {
        fun onReportAdded(report: Report)
    }

    private var listener: OnReportAddedListener? = null
    private var _binding: AddReportDialogBinding? = null
    private val binding get() = _binding!!

    private val reportsViewModel: ReportsViewModel by activityViewModels()
    private lateinit var imagePickerUtil: ImagePickerUtil

    private var capturedImage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddReportDialogBinding.inflate(inflater, container, false)

        imagePickerUtil = ImagePickerUtil(this,
            onImagePicked = { uri ->
                uri.let {
                    binding.reportImage.setImageURI(it)
                    capturedImage = uri.toString()
                }
            },
            onImageUploadResult = { success, downloadUrl ->
                if (success && downloadUrl != null) {
                    // Handle the upload success if needed, for example:
                    Log.d(TAG, "Image uploaded successfully: $downloadUrl")
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        )


        binding.reportImage.setOnClickListener {
            showImagePickerDialog()
        }

        binding.reportNow.setOnClickListener {
            saveReport()
        }

        return binding.root
    }

    private fun showImagePickerDialog() {
        val items = arrayOf<CharSequence>("Take Photo", "Choose from Library", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Add Photo!")
        builder.setItems(items) { dialog, item ->
            when (items[item]) {
                "Take Photo" -> {
                    imagePickerUtil.requestCaptureImagePermission()
                    dialog.dismiss()
                }
                "Choose from Library" -> {
                    imagePickerUtil.pickImage()
                    dialog.dismiss()
                }
                "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun saveReport() {
        val description = binding.reportDescription.text.toString().trim()
//        val reporterName = UserUtils.getFullName(UserManager.getUser())
        val reporter = UserManager.getUser()?.id
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        if (description.isNotEmpty() && capturedImage != null) {
            val report = Report(
                photo=capturedImage,
                description = description,
                reporter = reporter,
                timestamp = timestamp
            )
//            val report = Report(
//                capturedImage!!,
//                description,
//                "Reported By: $reporterName",
//                timestamp)
            listener?.onReportAdded(report)
            dismiss()
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    fun setOnReportAddedListener(listener: OnReportAddedListener) {
        this.listener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}
