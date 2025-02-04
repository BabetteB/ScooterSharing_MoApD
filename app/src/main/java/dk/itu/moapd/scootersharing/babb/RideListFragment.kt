package dk.itu.moapd.scootersharing.babb

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType.Companion.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.databinding.FragmentRideListBinding
import dk.itu.moapd.scootersharing.babb.model.*
import dk.itu.moapd.scootersharing.babb.viewmodel.RideListViewModel
import dk.itu.moapd.scootersharing.babb.viewmodel.StartRideFragment
import dk.itu.moapd.scootersharing.babb.viewmodel.UpdateRideFragment


class RideListFragment : Fragment(), ItemClickListener {

    private lateinit var database: DatabaseReference
    private lateinit var auth : FirebaseAuth



    companion object{
        private lateinit var DATABASE_URL: String
        private lateinit var rideListViewModel: RideListViewModel
    }

    private var _binding: FragmentRideListBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rideListViewModel = ViewModelProvider(this).get(RideListViewModel::class.java)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                RideListScreen(viewModel = rideListViewModel)
            }
        }



//        _binding = FragmentRideListBinding.inflate(inflater, container, false)
//        binding.rideRecyclerView.layoutManager = LinearLayoutManager(context)

        // get all scooters and sort by location
//        val query = database
//            .child("scooter")
//
//        val options =
//            FirebaseRecyclerOptions.Builder<Scooter>()
//                .setQuery(query, Scooter::class.java)
//                .setLifecycleOwner(this)
//                .build()
//
//        adapter = CustomAdapter(this, options)
//
//        updateBinding(adapter)
//        return binding.root
    }

    @Composable
    fun RideListScreen(viewModel: RideListViewModel) {
        val scooters by viewModel.scooters.observeAsState(emptyList())

        LazyColumn (modifier = Modifier.fillMaxWidth()){
            items(scooters) { scooter ->
                ScooterItem(scooter = scooter)
            }
        }
    }

    @Composable
    fun ScooterItem(scooter: Scooter) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ){
            Image(
                painterResource(R.drawable.scooter),
                contentDescription = "Scooter Icon",
                modifier = Modifier.size(110.dp).align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterVertically)) {
                Text(text = "Name: " + scooter.name!!, style = MaterialTheme.typography.body1)
                Text(text = "Reserved: " + scooter.reserved.toString(), style = MaterialTheme.typography.body1)
                // Display other scooter info here as needed
            }
            Spacer(modifier = Modifier.width(10.dp))
             Button(onClick = {},
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(6.dp)
                     .align(Alignment.CenterVertically),
                 colors = ButtonDefaults.buttonColors(colorResource(R.color.primary_purple))
             )
             {
                 Text(text= "Start ride!"
                 )
             }
        }

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRideClicked(scooterId: String, scooterName : String) {
        findNavController().navigate(
            RideListFragmentDirections.showUpdateRide(scooterId, scooterName)
        )
    }


}