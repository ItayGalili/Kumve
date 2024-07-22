package il.co.erg.mykumve.ui.reports

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.firebasemvm.util.Status
import il.co.erg.mykumve.data.db.model.Report
import il.co.erg.mykumve.databinding.ReportsItemBinding
import il.co.erg.mykumve.ui.viewmodel.UserViewModel
import il.co.erg.mykumve.util.UserUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReportsAdapter(
    private var reports: MutableList<Report>,
    private var userViewModel: UserViewModel,
    private val lifecycleOwner: LifecycleOwner,
    var context: Context,
    ) :
    RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(private val binding: ReportsItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(report: Report) {
            // Load image using Glide
            Glide.with(binding.root.context)
                .load(report.photo)
                .into(binding.reportImage)

            report.reporter?.let { reporter ->
                lifecycleOwner.lifecycleScope.launch {
                    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        userViewModel.fetchUserById(reporter).collectLatest { userResource ->
                            if (userResource.status == Status.SUCCESS) {
                                // Set reporter name
                                binding.reportUser.text = "${context.getString(R.string.reported_by)}: ${UserUtils.getFullName(userResource.data)}"
                            } else {
                                binding.reportUser.text = context.getString(R.string.unkown_reporter)
                            }
                        }
                    }
                }
            }

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
