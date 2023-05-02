package dk.itu.moapd.scootersharing.babb.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import dk.itu.moapd.scootersharing.babb.databinding.ListItemHistoryBinding
import dk.itu.moapd.scootersharing.babb.databinding.ListItemRideBinding

class HistoryHolder(private val binding: ListItemHistoryBinding)
    : RecyclerView.ViewHolder(binding.root){
    fun bind (scooter : Scooter) {
        with (binding) {
            historyName.text = scooter.name
            //historyFrom.text = scooter.location
            //historyTo.text = scooter.location
            historyTime.text = scooter.lastUpdateTimeStamp.toString()
            historyPrice.text = "100 DKK"
        }

    }
}

class HistoryAdapter(options : FirebaseRecyclerOptions<Scooter>)
    : FirebaseRecyclerAdapter<Scooter, HistoryHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemHistoryBinding.inflate(inflater, parent, false)
        return HistoryHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int, model: Scooter) {
        holder.bind(model)
    }


}