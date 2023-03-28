/*MIT License

Copyright (c) [2023] [Babette & Freyja]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.*/

package dk.itu.moapd.scootersharing.babb.viewmodel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.RideListFragment
import dk.itu.moapd.scootersharing.babb.databinding.ActivityMainBinding
import dk.itu.moapd.scootersharing.babb.model.CustomAdapter
import dk.itu.moapd.scootersharing.babb.model.ItemClickListener
import dk.itu.moapd.scootersharing.babb.model.Scooter
import java.util.UUID


/**
 * The MainActivity is the sole activity, used for storing the MainFragment
 */
class MainActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var navController : NavController
    /**
     * Binding view and activity
     */
    private lateinit var mainBinding : ActivityMainBinding

    private var user : FirebaseUser? = null
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference

    companion object {
        val TAG = "MAINACTIVITY"
        private lateinit var DATABASE_URL: String
        private lateinit var adapter: CustomAdapter
    }

    /**
     * upon creating the instance of main activity, inflate the binding (see activity_main.xml)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        Firebase.database(DATABASE_URL).setPersistenceEnabled(true)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        // Initialize Firebase database
        database = Firebase.database(DATABASE_URL).reference

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        setupWithNavController(bottomNav, navController)

        setSupportActionBar(findViewById(R.id.main_toolbar))

    }



    override fun onStart() {
        super.onStart()


        if (auth.currentUser == null)
            startLoginActivity()
    }



    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    private fun updateScooterLocation(location : String) {
        auth.currentUser?.let {
            database.child("scooters")
                .child(it.uid)
                .child("location")
                .setValue(location)
        }
    }



    private fun deleteScooter(scooterRef : DatabaseReference) {
        //TODO: add method for checking this
        auth.currentUser?.let { user ->
            val uid = database.child("scooters")
                .child(user.uid)
                .push()
                .key

            // Insert the object in the database.
            uid?.let {
                database.child("scooters")
                    .child(user.uid)
                    .child(it)
                    .setValue(null)//TODO: find a way to use removeValue() insted (have to reference location of the data but how?)
                    .addOnSuccessListener {
                        // Write was successful!
                        // ...
                    }
                    .addOnFailureListener {
                        // Write failed
                        // ...
                    }
            }
        }
    }


    override fun onRideClicked(scooterId: String) {
        TODO("Not yet implemented")
    }

    override fun onRideLongClicked(scooterId: String) {
        TODO("Not yet implemented")
    }


}