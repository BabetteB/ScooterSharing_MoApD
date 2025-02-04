package dk.itu.moapd.scootersharing.babb.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.model.Scooter

class RideListViewModel : ViewModel() {

    private val _scooters = MutableLiveData<List<Scooter>>()
    val scooters: LiveData<List<Scooter>> = _scooters

    private val database = Firebase.database.reference

    init {
        fetchScooters()
    }

    private fun fetchScooters() {
        database.child("scooter").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val scooterList = snapshot.children.mapNotNull { it.getValue(Scooter::class.java) }
                _scooters.value = scooterList
            }
        }
    }
}
