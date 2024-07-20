package il.co.erg.mykumve.ui.reports

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import il.co.erg.mykumve.data.model.Report
import il.co.erg.mykumve.databinding.AddReportDialogBinding
import il.co.erg.mykumve.util.ImagePickerUtil
import il.co.erg.mykumve.util.UserManager
import il.co.erg.mykumve.util.UserUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddReportDialogFragment : DialogFragment() {

    interface OnReportAddedListener {
        fun onReportAdded(report: Report)
    }

    private var listener: OnReportAddedListener? = null
    private var _binding: AddReportDialogBinding? = null
    private val binding get() = _binding!!

    private val reportsViewModel: ReportsViewModel by activityViewModels()
    private lateinit var imagePickerUtil: ImagePickerUtil

    private var capturedImageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = AddReportDialogBinding.inflate(inflater, container, false)

        imagePickerUtil = ImagePickerUtil(this) { uri ->
            uri?.let {
                binding.reportImage.setImageURI(it)
                capturedImageBitmap = uriToBitmap(it)
            }
        }

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
        val reporterName = UserUtils.getFullName(UserManager.getUser())
        val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        if (description.isNotEmpty() && capturedImageBitmap != null) {
            val report = Report(capturedImageBitmap!!, description, "Reported By: $reporterName", timestamp)
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
