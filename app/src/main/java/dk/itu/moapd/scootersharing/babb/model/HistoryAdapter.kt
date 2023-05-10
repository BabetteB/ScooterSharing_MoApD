package dk.itu.moapd.scootersharing.babb.model

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.scootersharing.babb.databinding.ListItemHistoryBinding
import dk.itu.moapd.scootersharing.babb.databinding.ListItemRideBinding
import dk.itu.moapd.scootersharing.babb.viewmodel.HistoryFragment
import dk.itu.moapd.scootersharing.babb.viewmodel.ScooterFragment

class HistoryHolder(private val binding: ListItemHistoryBinding)
    : RecyclerView.ViewHolder(binding.root){
    fun bind (scooter : Scooter) {
        with (binding) {
            historyName.text = scooter.name
            historyTime.text = scooter.lastUpdateTimeStamp.toString()
            historyPrice.text = "100 DKK"
        }

    }
}

class HistoryAdapter(options : FirebaseRecyclerOptions<Scooter>)
    : FirebaseRecyclerAdapter<Scooter, HistoryHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        Log.d("HistoryAdapter", "Instance created")
        val inflater = LayoutInflater.from(parent.context)
        Log.d("HistoryAdapter", "inflater $inflater")
        val binding = ListItemHistoryBinding.inflate(inflater, parent, false)
        Log.d("HistoryAdapter", "binding $binding")
        return HistoryHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int, model: Scooter) {
        Log.d("HistoryAdapter", "about to bind scooter $model")
        holder.bind(model)
    }


}