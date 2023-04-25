package dk.itu.moapd.scootersharing.babb.viewmodel

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.FragmentScooterBinding
import dk.itu.moapd.scootersharing.babb.databinding.FragmentStartRideBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ScooterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ScooterFragment : Fragment() {

    private var _binding : FragmentScooterBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Oh no I died"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScooterBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with (binding) {

        }
    }

}