package com.example.mykumve.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.mykumve.R
import com.example.mykumve.data.model.Report

class AddReportDialogFragment : DialogFragment() {

    // Define interface for communication with parent fragment
    interface OnReportAddedListener {
        fun onReportAdded(report: Report)
    }

    private var listener: OnReportAddedListener? = null
    private lateinit var reportImage: ImageView
    private lateinit var descriptionEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_report_dialog, container, false)

        reportImage = view.findViewById(R.id.report_image)
        descriptionEditText = view.findViewById(R.id.report_description)

        // Capture image button click
        reportImage.setOnClickListener {
            captureImage()
        }

        // Save button click
        val saveButton = view.findViewById<Button>(R.id.report_now)
        saveButton.setOnClickListener {
            saveReport()
            dismiss()
        }

        return view
    }

    private fun captureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the captured image
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Display the captured image
            reportImage.setImageBitmap(imageBitmap)
            capturedImageBitmap = imageBitmap
        }
    }

    private var capturedImageBitmap: Bitmap? = null

    private fun saveReport() {
        val description = descriptionEditText.text.toString().trim()
        if (description.isNotEmpty() && capturedImageBitmap != null) {
            // Create a Report object with captured image, description, reporter name, and timestamp
            val report = Report(capturedImageBitmap!!, description, "Reporter Name")

            // Notify parent fragment (UsersReports) through the listener
            listener?.onReportAdded(report)
        }
    }

    fun setOnReportAddedListener(listener: OnReportAddedListener) {
        this.listener = listener
    }

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
    }
}