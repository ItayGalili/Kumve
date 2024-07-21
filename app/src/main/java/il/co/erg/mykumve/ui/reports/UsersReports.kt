package il.co.erg.mykumve.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.Report
import il.co.erg.mykumve.databinding.ReportsBinding
import il.co.erg.mykumve.ui.reports.reports.ReportsAdapter
import il.co.erg.mykumve.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UsersReports : Fragment(), AddReportDialogFragment.OnReportAddedListener {

    private var _binding: ReportsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportsViewModel by activityViewModels()
    private lateinit var reportsAdapter: ReportsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ReportsBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()
        setupButtons()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        reportsAdapter = ReportsAdapter(mutableListOf())
        binding.recyclerView.adapter = reportsAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reports.collectLatest { reports ->
                    reportsAdapter.updateReports(reports)
                }
            }
        }
    }

    private fun setupButtons() {
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
    }

    override fun onReportAdded(report: Report) {
        report.imageBitmap?.let { bitmap ->
            viewModel.addReport(bitmap, report.description, report.reporter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
