package il.co.erg.mykumve.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
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
                _operationResult.value = Resource.success("Reports fetched successfully")
            } else {
                _operationResult.value = Resource.error(result.message ?: "Failed to fetch reports")
            }
        }
    }

    fun addReport(imageBitmap: Bitmap, description: String, reporter: String) {
        viewModelScope.launch {
            _operationResult.value = Resource.loading()
            val report = Report(imageBitmap, description, reporter)
            val result = reportRepository.addReport(report)
            _operationResult.value = result
            if (result.status == Status.SUCCESS) {
                fetchReports() // Refresh the reports list
            }
        }
    }
}
