package dk.itu.moapd.scootersharing.babb.model

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dk.itu.moapd.scootersharing.babb.R

class Application : Application() {
    private lateinit var DATABASE_URL: String

    override fun onCreate() {
        super.onCreate()
        DATABASE_URL = resources.getString(R.string.DATABASE_URL)
        Firebase.database(DATABASE_URL).setPersistenceEnabled(true)
    }

}