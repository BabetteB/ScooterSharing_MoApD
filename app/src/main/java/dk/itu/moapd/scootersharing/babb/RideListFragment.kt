package dk.itu.moapd.scootersharing.babb

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dk.itu.moapd.scootersharing.babb.databinding.FragmentRideListBinding
import dk.itu.moapd.scootersharing.babb.model.ItemClickListener
import dk.itu.moapd.scootersharing.babb.model.RideListAdapter
import dk.itu.moapd.scootersharing.babb.model.RidesDB
import dk.itu.moapd.scootersharing.babb.model.Scooter
import dk.itu.moapd.scootersharing.babb.viewmodel.StartRideFragment
import dk.itu.moapd.scootersharing.babb.viewmodel.UpdateRideFragment
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "RideListFragment"

class RideListFragment : Fragment(), ItemClickListener {

    private var _binding: FragmentRideListBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding."
        }

    companion object {
        lateinit var ridesDB : RidesDB
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ridesDB = RidesDB.get(this.requireActivity())

        //setHasOptionsMenu(true)
        Log.d(TAG, "Total rides: ${ridesDB.getRidesList().size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRideListBinding.inflate(inflater, container, false)
        binding.rideRecyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = makeAdapter(ridesDB.getRidesList(), this)

        updateBinding(adapter)

        return binding.root
    }

    private fun makeAdapter(list : List<Scooter>, itemClickListener: ItemClickListener) : RideListAdapter {
        return RideListAdapter(list, itemClickListener)
    }

    private fun updateBinding(adapter : RideListAdapter) {
        binding.rideRecyclerView.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener(
            StartRideFragment.REQUEST_KEY_NEW_SCOOTER
        ) {
            _, bundle ->
            val newScooter = bundle.getSerializable(StartRideFragment.BUNDLE_KEY_NEW_SCOOTER) as Scooter
            ridesDB.addScooter(newScooter)
        }

        setFragmentResultListener(
            UpdateRideFragment.REQUEST_KEY_UPDATED_SCOOTER_LOCATION
        ) {
                _, bundle ->
            val newLocation = bundle.getSerializable(UpdateRideFragment.BUNDLE_KEY_UPDATED_SCOOTER_LOCATION) as Scooter
            ridesDB.updateScooterLocation(UUID.randomUUID(), newLocation.location)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_new_ride, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_ride -> {
                showNewRide()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNewRide() {
        viewLifecycleOwner.lifecycleScope.launch {
            findNavController().navigate(
                RideListFragmentDirections.showStartRide()
            )
        }
    } */

    override fun onRideClicked(scooterId: String) {
        findNavController().navigate(
            RideListFragmentDirections.showUpdateRide(scooterId)
        )
    }

    override fun onRideLongClicked(scooterId: String) {
        /*ridesDB.deleteScooter(scooterId)
        Toast.makeText(
            context,
            "$scooterId deleted",
            Toast.LENGTH_SHORT
        ).show()

        var adapter = makeAdapter(ridesDB.getRidesList(), this)
        updateBinding(adapter)*/
    }

}