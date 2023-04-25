package dk.itu.moapd.scootersharing.babb.model

import android.R
import android.util.Log
import android.widget.Chronometer
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dk.itu.moapd.scootersharing.babb.viewmodel.MainActivity


class ScooterViewModel : ViewModel() {
    private lateinit var scooters : ArrayList<Scooter>

    companion object{
        private const val TAG = "ScooterViewModel"
        var userScooter : Scooter? = null
    }

    fun getUserScooter() : Scooter? {
        return userScooter
    }

    fun setUserScooter(scooter: Scooter, userID : String){
        userScooter = scooter
        scooter.assignedToUserID = userID

    }


    fun getCurrentUser() : FirebaseUser? {
        return MainActivity.currentUser
    }

    fun getScooters() : ArrayList<Scooter> {
        scooterListener()
        return scooters
    }


    fun getDB() : DatabaseReference{
        return MainActivity.database
    }

    private fun scooterListener(){
        val database = getDB()

        database.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        for (s in snapshot.children) {
                            val scooter = s.getValue(Scooter::class.java)
                            scooters.add(scooter!!)
                        }
                    }

                    Log.d(TAG, (snapshot.value as HashMap<*, *>).toString())
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            }
        )

    }

}