package com.example.mykumve.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mykumve.R
import com.example.mykumve.data.model.Report

class UsersReports : Fragment(), AddReportDialogFragment.OnReportAddedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reportsAdapter: ReportsAdapter
    private val reportsList = mutableListOf<Report>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reports, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        reportsAdapter = ReportsAdapter(reportsList)
        recyclerView.adapter = reportsAdapter

        // Add button click to open AddReportDialogFragment
        val addButton = view.findViewById<Button>(R.id.add_report)
        addButton.setOnClickListener {
            val dialogFragment = AddReportDialogFragment()
            dialogFragment.setOnReportAddedListener(this)
            dialogFragment.show(childFragmentManager, "AddReportDialogFragment")
        }

        return view
    }

    override fun onReportAdded(report: Report) {
        // Add the new report to the list and notify the adapter
        reportsList.add(report)
        reportsAdapter.notifyItemInserted(reportsList.size - 1)
    }
}

//package com.example.mykumve.ui
//
//import AddReportDialogFragment
//import ReportsAdapter
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.mykumve.R
//import com.example.mykumve.data.model.Report
//
//class UsersReports : Fragment() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var reportsAdapter: ReportsAdapter
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.reports, container, false)
//
//        recyclerView = view.findViewById(R.id.recyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//
//        // Dummy data for testing
//        val dummyReports = generateDummyReports()
//        reportsAdapter = ReportsAdapter(dummyReports)
//        recyclerView.adapter = reportsAdapter
//
//        return view
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val addReportButton = view.findViewById<Button>(R.id.add_report)
//        addReportButton.setOnClickListener {
//            showAddReportDialog()
//        }
//    }
//
//    private fun generateDummyReports(): List<Report> {
//        // Replace with actual data fetching logic
//        return listOf(
//            Report("url_to_image_1", "Description 1", "reporter 1"),
//            Report("url_to_image_2", "Description 2",  "reporter 2"),
//            Report("url_to_image_3", "Description 3",  "reporter 3")
//        )
//    }
//
//    private fun showAddReportDialog() {
//        val dialog = AddReportDialogFragment()
//        dialog.show(parentFragmentManager, "AddReportDialogFragment")
//    }
//
//
//}
//
//
////package com.example.mykumve.ui
////
////import ReportsAdapter
////import android.os.Bundle
////import android.view.LayoutInflater
////import android.view.View
////import android.view.ViewGroup
////import androidx.fragment.app.Fragment
////import androidx.recyclerview.widget.LinearLayoutManager
////import androidx.recyclerview.widget.RecyclerView
////import com.example.mykumve.R
////import com.example.mykumve.data.model.Report
////
////class UsersReports : Fragment() {
////
////    private lateinit var recyclerView: RecyclerView
////    private lateinit var reportAdapter: ReportsAdapter
////    private lateinit var reportList: List<Report>
////
////    override fun onCreateView(
////        inflater: LayoutInflater, container: ViewGroup?,
////        savedInstanceState: Bundle?
////    ): View? {
////        val view = inflater.inflate(R.layout.reports, container, false)
////
////        // Initialize RecyclerView
////        recyclerView = view.findViewById(R.id.recyclerView)
////        recyclerView.layoutManager = LinearLayoutManager(requireContext())
////
////        // Initialize report list (Replace with actual data fetching logic)
////        reportList = listOf(
////            Report("https://example.com/image1.jpg", "Description 1"),
////            Report("https://example.com/image2.jpg", "Description 2")
////        )
////
////        // Initialize and set up the adapter
////        reportAdapter = ReportsAdapter(reportList)
////        recyclerView.adapter = reportAdapter
////
////        return view
////    }
////}