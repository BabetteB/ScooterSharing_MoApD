package dk.itu.moapd.scootersharing.babb.model

import com.google.firebase.database.Query
import java.lang.Exception
import java.util.UUID

class ScooterDB {

    companion object{
        private var db : ScooterDB? = null

        fun getDB () : ScooterDB {
            if (db != null)
                return db!!
            throw Exception("Not implemented")
        }

        fun queryDB(query: Query) {

        }

        fun getAllScooters() : List<Scooter> {
            throw Exception("Not implemented")
        }

        fun getScooter(id : UUID) : Scooter {
            throw Exception("Not implemented")
        }


    }

}