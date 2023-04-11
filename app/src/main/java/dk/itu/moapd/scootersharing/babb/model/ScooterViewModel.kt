package dk.itu.moapd.scootersharing.babb.model

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.viewmodel.MainActivity

class ScooterViewModel : ViewModel() {
    private lateinit var adapter : CustomAdapter
    private lateinit var auth : FirebaseAuth
    private lateinit var scooters : List<Scooter>
    lateinit var DATABASE_URL: String


    companion object{
        private val TAG = "ScooterViewModel"


    }


    fun getDB() : DatabaseReference{
        return MainActivity.database
    }

    fun getScooters() {
        var database = getDB()

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