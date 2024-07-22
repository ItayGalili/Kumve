package il.co.erg.mykumve.ui.explore
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.databinding.TripInfoCardBinding
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel
import il.co.erg.mykumve.ui.viewmodel.TripViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExploreAdapter(
    var tripsInfo: MutableList<TripInfo>,
    private val sharedViewModel: SharedTripViewModel,
    private val tripViewModel: TripViewModel,
    var context: Context,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<ExploreAdapter.TripInfoViewHolder>() {

    private var filteredTripList: MutableList<TripInfo> = tripsInfo.toMutableList()
    inner class TripInfoViewHolder(private val binding: TripInfoCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tripInfo: TripInfo) {

            if(sharedViewModel.isNavigatedFromExplore){
                binding.checkBox.isVisible=false
                binding.expandTripInfo.isVisible=true
            }
            else{
                binding.checkBox.isVisible=true
                binding.expandTripInfo.isVisible=false
            }
            var checkBoxCunter=0
            if(binding.checkBox.isVisible){
                binding.checkBox.setOnClickListener(){
                    checkBoxCunter=checkBoxCunter+1
                }
            }
            sharedViewModel.isExactlyOneTripIsChecked = checkBoxCunter == 1

            binding.tripInfoDifficulty.text=context.getString(tripInfo.difficulty.prettyString())
            binding.tripInfoTitle.text = tripInfo.title
            if(tripInfo.length?.isNaN() == false){
                binding.tripInfoLength.text= buildString {
                    append((tripInfo.length.toString()))
                    append(" km")
                }
            }
            else{
                binding.tripInfoLength.text="Length is not specified"
            }
            // Extract image URL and alt text from the map, handling null cases
            val imageUrl = tripInfo.imageInfo?.get("src") ?: ""
            val imageAltText = tripInfo.imageInfo?.get("alt") ?: ""
            // Load the image using Glide
            Glide.with(context)
                .load(imageUrl) // Use the image URL
                .placeholder(R.drawable.my_alerts) // Placeholder image
                .error(R.drawable.my_alerts) // Error image
                .into(binding.tripInfoImage)
            // Set the content description for accessibility, handle empty alt text
            binding.tripInfoImageDescription.setText(imageAltText)
            binding.tripInfoImage.contentDescription = if (imageAltText.isNullOrBlank()) "Image" else imageAltText

            lifecycleOwner.lifecycleScope.launch {
                val subArea = tripViewModel.allSubAreas.collectLatest { it.find { it.nameKey == tripInfo.subAreaId }}
                binding.tripInfoSubarea.text = subArea.toString()
                val area = tripViewModel.allAreas.collectLatest { it.find  { it.nameKey == tripInfo.areaId }}
                binding.tripInfoArea.text = area.toString()
            }

            binding.expandTripInfo.setOnClickListener {
                sharedViewModel.isNavigatedFromExplore = true
                sharedViewModel.selectTripInfo(tripInfo)
                it.findNavController().navigate(R.id.action_exploreFragment_to_expendedTripInfoFragment)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripInfoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TripInfoCardBinding.inflate(inflater, parent, false)
        return TripInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripInfoViewHolder, position: Int) {
        val trip = filteredTripList[position]
        holder.bind(trip)
    }

    override fun getItemCount(): Int {
        return filteredTripList.size
    }

    fun updateTripList(newTripList: List<TripInfo>) {
        tripsInfo = newTripList.toMutableList()
        filteredTripList = tripsInfo.toMutableList()
        notifyDataSetChanged()
    }

    fun filterByQuery(query: String) {
        filteredTripList = if (query.isEmpty()) {
            tripsInfo.toMutableList()
        } else {
            tripsInfo.filter { tripInfo ->
                tripInfo.areaId.toString().contains(query, ignoreCase = true) ||
                        tripInfo.title.contains(query, ignoreCase = true) ||
                        tripInfo.length.toString().contains(query, ignoreCase = true) ||
                        tripInfo.subAreaId.toString().contains(query, ignoreCase = true) ||
                        tripInfo.description?.contains(query, ignoreCase = true) == true || tripInfo.difficulty.toString().contains(query,ignoreCase=true)

            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}