
package com.example.mykumve.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mykumve.R
import com.example.mykumve.data.model.Report
import com.example.mykumve.databinding.ReportsBinding

class UsersReports : Fragment(), AddReportDialogFragment.OnReportAddedListener {
    private var _binding: ReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reportsAdapter: ReportsAdapter
    private lateinit var viewModel: ReportsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ReportsBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel = ViewModelProvider(requireActivity()).get(ReportsViewModel::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize adapter with current list of reports
        reportsAdapter = ReportsAdapter(viewModel.getReports().toMutableList())
        binding.recyclerView.adapter = reportsAdapter

        // Add button click to open AddReportDialogFragment
        binding.addReport.setOnClickListener {
            val dialogFragment = AddReportDialogFragment()
            dialogFragment.setOnReportAddedListener(this)
            dialogFragment.show(childFragmentManager, "AddReportDialogFragment")
        }
        binding.partnersBtnMs.setOnClickListener {
            findNavController().navigate(R.id.action_UsersReports_to_networkManager)
        }
        binding.msBtn.setOnClickListener {
            findNavController().navigate(R.id.action_UsersReports_to_mainScreenManager)
        }

        return view
    }

    override fun onReportAdded(report: Report) {
        // Add the new report to the view model
        viewModel.addReport(report)

        // Notify adapter that a new item has been inserted
        reportsAdapter.addReport(report)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}