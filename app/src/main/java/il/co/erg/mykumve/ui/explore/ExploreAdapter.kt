package il.co.erg.mykumve.ui.explore
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import il.co.erg.mykumve.R
import il.co.erg.mykumve.data.db.model.TripInfo
import il.co.erg.mykumve.databinding.TripInfoCardBinding
import il.co.erg.mykumve.ui.viewmodel.SharedTripViewModel

class ExploreAdapter(
var tripsInfo: MutableList<TripInfo>,
    private val sharedViewModel: SharedTripViewModel,
    var context: Context,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<ExploreAdapter.TripInfoViewHolder>() {

    private var filteredTripList: MutableList<TripInfo> = tripsInfo.toMutableList()
    inner class TripInfoViewHolder(private val binding: TripInfoCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tripInfo: TripInfo) {
            binding.tripInfoDescription.text = tripInfo.description
            binding.tripInfoDifficulty.text=tripInfo.difficulty.toString()
            binding.tripInfoTitle.text = tripInfo.title
            //binding.tripInfoSubarea.text =
            //binding.tripInfoArea.text =
            if(tripInfo.length?.isNaN() == false){
                binding.tripInfoLength.text= buildString {
                    append((tripInfo.length.toString()))
                    append(" km")
                }
            }
            else{
                binding.tripInfoLength.text="Length is not specified"
            }
            if(tripInfo.publishedDate == null){
                binding.tripInfoCreationDate.text= tripInfo.publishedDate
            }
            else{
                binding.tripInfoCreationDate.text="No Publish Date"
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
            binding.tripInfoImage.contentDescription = if (imageAltText.isNullOrBlank()) "Image" else imageAltText
            binding.addTripInfoCardToMyTrips.setOnClickListener {
                sharedViewModel.isNavigatedFromExplore = true
                it.findNavController().navigate(R.id.action_exploreFragment_to_travelManager)
            }
            binding.expandTripInfo.setOnClickListener {
                it.findNavController().navigate(R.id.action_exploreFragment_to_tripReview)
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



