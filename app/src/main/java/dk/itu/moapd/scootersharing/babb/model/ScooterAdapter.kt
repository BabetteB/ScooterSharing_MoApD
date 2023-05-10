package dk.itu.moapd.scootersharing.babb.model

import android.R
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.scootersharing.babb.RideListFragmentDirections
import dk.itu.moapd.scootersharing.babb.databinding.ListItemRideBinding
import java.util.*

class ScooterHolder(
    private val binding: ListItemRideBinding,
    private val itemClickListener: ItemClickListener
) : RecyclerView.ViewHolder(binding.root){

    fun bind (scooter : Scooter) {
        with (binding) {

            scooterName.text = scooter.name
            //scooterLocation.text = scooter.location
            scooterLastUpdate.text = scooter.lastUpdateTimeStamp.toString()

            cardView.setOnClickListener {
                itemClickListener.onRideClicked(scooter.id!!, scooter.name!!)
            }

            startRideButton.setOnClickListener {
                binding.root.findNavController().navigate(
                    RideListFragmentDirections.showQrscan()
                )
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
        holder.bind(scooter)
    }

}