package il.co.erg.mykumve.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import il.co.erg.mykumve.data.db.firebasemvm.repository.ReportRepository
import il.co.erg.mykumve.data.db.firebasemvm.util.Resource
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.model.Report
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportsViewModel(application: Application) : AndroidViewModel(application) {
    val TAG = ReportsViewModel::class.java.simpleName
    private val reportRepository = ReportRepository()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> get() = _reports

    private val _operationResult = MutableStateFlow<Resource<String>?>(null)
    val operationResult: StateFlow<Resource<String>?> get() = _operationResult

    fun fetchReports() {
        viewModelScope.launch {
            _operationResult.value = Resource.loading()
            val result = reportRepository.getReports()
            if (result.status == Status.SUCCESS) {
                _reports.value = result.data ?: emptyList()
                Log.d(TAG, "Reports fetched successfully: ${_reports.value.size} reports")
                _operationResult.value = Resource.success("Reports fetched successfully")
            } else {
                Log.e(TAG, "Failed to fetch reports: ${result.message}")
                _operationResult.value = Resource.error(result.message ?: "Failed to fetch reports")
            }
        }
    }

    fun addReport(report: Report) {
        viewModelScope.launch {
            _operationResult.value = Resource.loading()
//            val report = Report(
//                photo=report.photo,
//                description = report.description,
//                reporter = report.reporter,
//                timestamp = report.timestamp)

            val result = reportRepository.addReport(report)
            _operationResult.value = result
            if (result.status == Status.SUCCESS) {
                fetchReports() // Refresh the reports list
            }
        }
    }
}
