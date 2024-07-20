package il.co.erg.mykumve.ui.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.local_db.model.Report

class ReportsAdapter(private var reports: MutableList<Report>) :
    RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reportImage: ImageView = itemView.findViewById(R.id.report_image)
        private val reportUser: TextView = itemView.findViewById(R.id.report_user)
        private val reportDescription: TextView = itemView.findViewById(R.id.report_description)
        private val reportTimeStamp: TextView = itemView.findViewById(R.id.report_time_stamp)

        fun bind(report: Report) {
            // Load image using Glide
            Glide.with(itemView.context)
                .load(report.imageBitmap)
                .into(reportImage)

            // Set reporter name
            reportUser.text = report.reporter

            // Set description
            reportDescription.text = report.description

            // Set timestamp
            reportTimeStamp.text = report.timestamp
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reports_item, parent, false)
        return ReportViewHolder(view)
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
}