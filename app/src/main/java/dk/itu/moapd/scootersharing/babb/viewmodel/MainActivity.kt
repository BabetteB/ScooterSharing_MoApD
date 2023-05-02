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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R
import dk.itu.moapd.scootersharing.babb.databinding.ActivityMainBinding
import dk.itu.moapd.scootersharing.babb.model.*



/**
 * The MainActivity is the sole activity, used for storing the MainFragment
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController : NavController
    private lateinit var DATABASE_URL: String
    private lateinit var vm : ScooterViewModel


    /**
     * Binding view and activity
     */
    private lateinit var mainBinding : ActivityMainBinding

    private lateinit var auth : FirebaseAuth

    companion object {
        lateinit var database: DatabaseReference
        lateinit var currentUser : FirebaseUser
    }

    /**
     * upon creating the instance of main activity, inflate the binding (see activity_main.xml)
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase database
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference

        vm = ScooterViewModel()

        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()


        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        setupWithNavController(bottomNav, navController)
        setSupportActionBar(findViewById(R.id.top_toolbar))

    }


    override fun onStart() {
        super.onStart()

        if (auth.currentUser == null)
            startLoginActivity()

        currentUser = auth.currentUser!!
    }



    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }




}