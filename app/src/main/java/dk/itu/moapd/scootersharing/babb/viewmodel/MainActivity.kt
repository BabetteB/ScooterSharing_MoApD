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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var mainBinding : ActivityMainBinding
    lateinit var auth : FirebaseAuth
    private lateinit var vm: ScooterViewModel

    companion object {
        lateinit var database: DatabaseReference
        lateinit var currentUser : FirebaseUser
        private const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
        )
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase database
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        database = Firebase.database(DATABASE_URL).reference
        vm = ViewModelProvider(this).get(ScooterViewModel::class.java)
        // Initialize Firebase Auth.
        auth = FirebaseAuth.getInstance()
        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        with (mainBinding) {
            signOut.setOnClickListener {
                Toast.makeText(
                    baseContext,
                    "Signing out",
                    Toast.LENGTH_SHORT
                ).show()
                startLogOutActivity()
            }
        }
        setContentView(mainBinding.root)

        @RequiresApi(Build.VERSION_CODES.S)
        if (!allPermissionsGranted())
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        setupWithNavController(bottomNav, navController)
    }


    override fun onStart() {
        super.onStart()

        if (auth.currentUser == null)
            startLoginActivity()
        else {
            currentUser = auth.currentUser!!
        }
    }


    fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun startLogOutActivity() {
        FirebaseAuth.getInstance().signOut()
        startLoginActivity()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


}