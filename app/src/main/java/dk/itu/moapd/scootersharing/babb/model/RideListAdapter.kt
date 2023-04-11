package dk.itu.moapd.scootersharing.babb.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dk.itu.moapd.scootersharing.babb.databinding.ListItemRideBinding

class RideHolder(
    private val binding: ListItemRideBinding,
    private val itemClickListener: ItemClickListener
 ) : RecyclerView.ViewHolder(binding.root){
     fun bind (scooter : Scooter) {
         with (binding) {
             scooterName.text = scooter.name
             scooterLocation.text = scooter.location
             scooterLastUpdate.text = scooter.lastUpdateTimeStamp.toString()

             cardView.setOnClickListener {
                 //itemClickListener.onRideClicked(scooter.name)
             }

             cardView.setOnLongClickListener {
                 //itemClickListener.onRideLongClicked(scooter.name)
                 true
             }

         }

     }
 }

class RideListAdapter (private val rides: List<Scooter>,
                       private val itemClickListener: ItemClickListener
)    : RecyclerView.Adapter<RideHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : RideHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemRideBinding.inflate(inflater, parent, false)
        return RideHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: RideHolder, position: Int) {
        val ride = rides[position]
        holder.bind(ride)
    }
    override fun getItemCount() = rides.size

}