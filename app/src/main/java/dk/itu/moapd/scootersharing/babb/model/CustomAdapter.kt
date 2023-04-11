package dk.itu.moapd.scootersharing.babb.model

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.scootersharing.babb.RideListFragmentDirections
import dk.itu.moapd.scootersharing.babb.databinding.ListItemRideBinding

class ScooterHolder(
    private val binding: ListItemRideBinding,
    private val itemClickListener: ItemClickListener
) : RecyclerView.ViewHolder(binding.root){
    fun bind (scooter : Scooter, position: Int) {
        with (binding) {
            scooterName.text = scooter.name
            scooterLocation.text = scooter.location
            scooterLastUpdate.text = scooter.lastUpdateTimeStamp.toString()

            cardView.setOnClickListener {
                itemClickListener.onRideClicked(scooter.name)
            }
            cardView.setOnLongClickListener {
                itemClickListener.onRideLongClicked(scooter.name)
                true
            }

        }

    }
}


class CustomAdapter(private val itemClickListener: ItemClickListener,
                    options : FirebaseRecyclerOptions<Scooter>)
    : FirebaseRecyclerAdapter<Scooter, ScooterHolder>(options){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) : ScooterHolder {
        Log.d("CustomAdapter", "Creating a new ViewHolder.")

        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemRideBinding.inflate(inflater, parent, false)
        return ScooterHolder(binding, itemClickListener)
    }

    override fun onBindViewHolder(holder: ScooterHolder, position: Int, scooter : Scooter) {
        holder.bind(scooter, position)
    }

}