package il.co.erg.mykumve.ui.reports

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
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
import il.co.erg.mykumve.ui.viewmodel.ReportsViewModel
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UsersReportsFragment : Fragment(), AddReportDialogFragment.OnReportAddedListener {
    val TAG = UsersReportsFragment::class.java.simpleName
    private var _binding: ReportsBinding? = null
    private val binding get() = _binding!!
    private val reportsViewModel: ReportsViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var reportsAdapter: ReportsAdapter
    private val sharedViewModel: SharedTripViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.UserReports)
        }

        _binding = ReportsBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupObservers()
        setupButtons()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        reportsAdapter = ReportsAdapter(
            mutableListOf(),
            userViewModel,
            viewLifecycleOwner,
            requireContext(),
            )
        binding.recyclerView.adapter = reportsAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                reportsViewModel.fetchReports()
                reportsViewModel.reports.collectLatest { reports ->
                    Log.d(TAG, "Received ${reports.size} reports")
                    reportsAdapter.updateReports(reports.sortedByDescending { it.timestamp })
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
            findNavController().navigate(R.id.action_UsersReports_to_exploreFragment)
        }
        binding.msBtn.setOnClickListener {
            findNavController().navigate(R.id.action_UsersReports_to_mainScreenManager)
        }
    }

    override fun onReportAdded(report: Report) {
        report.photo?.let { photo ->
            reportsViewModel.addReport(report)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
