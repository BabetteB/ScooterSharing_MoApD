package dk.itu.moapd.scootersharing.babb.viewmodel

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentHistoryBinding
import dk.itu.moapd.scootersharing.babb.model.HistoryAdapter
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.model.ScooterViewModel

class HistoryFragment : Fragment() {

    private lateinit var database: DatabaseReference
    lateinit var auth : FirebaseAuth
    lateinit var vm : ScooterViewModel

    var _binding: FragmentHistoryBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    companion object{
        const val TAG = "HistoryFragment"
        private lateinit var adapter : HistoryAdapter
        private lateinit var DATABASE_URL : String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Instance created.")

        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference
        auth = FirebaseAuth.getInstance()

        vm = ViewModelProvider(requireActivity()).get(ScooterViewModel::class.java)
        Log.d(TAG, "vm SCOOTER_ID : ${vm.activeScooterId}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(context)

        val query = database
            .child("history").child(auth?.uid.toString())

        val options =
            FirebaseRecyclerOptions.Builder<Scooter>()
                .setQuery(query, Scooter::class.java)
                .setLifecycleOwner(this)
                .build()


        adapter = HistoryAdapter(options)
        binding.historyRecyclerView.adapter = adapter


        return binding.root
    }

}