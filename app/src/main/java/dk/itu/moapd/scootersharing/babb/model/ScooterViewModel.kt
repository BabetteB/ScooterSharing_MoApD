package dk.itu.moapd.scootersharing.babb.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class ScooterViewModel : ViewModel() {
    private lateinit var adapter : CustomAdapter
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private lateinit var scooters : List<Scooter>

    companion object{
        private lateinit var DATABASE_URL: String
        private val TAG = "ScooterViewModel"
    }

    fun getScooters() {
        database.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    scooters = snapshot.value as List<Scooter>
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            }
        )
    }

}