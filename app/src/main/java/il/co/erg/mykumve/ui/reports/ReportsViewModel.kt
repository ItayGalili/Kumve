package il.co.erg.mykumve.ui.reports

import androidx.lifecycle.ViewModel
import il.co.erg.mykumve.data.model.Report

class ReportsViewModel : ViewModel() {
    private val reportsList = mutableListOf<Report>()

    fun getReports(): List<Report> {
        return reportsList
    }

    fun addReport(report: Report) {
        reportsList.add(report)
    }
}