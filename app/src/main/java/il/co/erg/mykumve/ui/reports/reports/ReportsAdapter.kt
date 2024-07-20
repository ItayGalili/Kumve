package il.co.erg.mykumve.ui.reports.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import il.co.erg.mykumve.data.db.model.Report
import il.co.erg.mykumve.databinding.ReportsItemBinding

class ReportsAdapter(private var reports: MutableList<Report>) :
    RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(private val binding: ReportsItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            // Load image using Glide
            Glide.with(binding.root.context)
                .load(report.imageBitmap)
                .into(binding.reportImage)

            // Set reporter name
            binding.reportUser.text = report.reporter

            // Set description
            binding.reportDescription.text = report.description

            // Set timestamp
            binding.reportTimeStamp.text = report.timestamp
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ReportsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position])
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    fun addReport(report: Report) {
        reports.add(report)
        notifyItemInserted(reports.size - 1)
    }

    fun updateReports(newReports: List<Report>) {
        reports.clear()
        reports.addAll(newReports)
        notifyDataSetChanged()
    }
}
