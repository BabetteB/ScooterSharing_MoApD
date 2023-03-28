package dk.itu.moapd.scootersharing.babb.model

import android.content.Context
import java.util.*
import kotlin.collections.ArrayList

class RidesDB private constructor(context: Context) {
    private val rides = ArrayList<Scooter>()
    private var count = 6u

    companion object : RidesDBHolder<RidesDB, Context>(::RidesDB)

    init {
        rides.add(
            Scooter(1u, "CPH001", "ITU", randomDate())
        )
        rides.add(
            Scooter(2u, "CPH002", "Fields", randomDate())
        )
        rides.add(
            Scooter(3u, "CPH003", "Lufthavn", randomDate())
        )
        rides.add(
            Scooter(4u, "CPH004", "Here", randomDate())
        )
        rides.add(
            Scooter(5u, "CPH005", "Over there", randomDate())
        )
        rides.add(
            Scooter(6u, "CPH006", "Somewhere", randomDate())
        )
    }

    fun getRidesList(): List<Scooter> {
        return rides
    }

    fun deleteScooter(scooter : Scooter) {
        rides.remove(scooter)
    }

    fun deleteScooter(scooterId : UInt) {
        val scooter = getScooter(scooterId)
        rides.remove(scooter)
    }

    fun addScooter(name: String, location: String) {
        count = count++
        rides.add(Scooter(count, name, location))
    }
    fun addScooter(scooter: Scooter) {
        rides.add(scooter)
    }

    private fun getScooter (sid : UInt) : Scooter {
        rides.forEach{ s ->
            if (s.id == sid){
                return s
            }
        }
        error("No scooter has that id")
    }

    fun updateScooterLocation(sid: UInt, location: String){
        val s = getScooter(sid)
        s.location = location
        s.lastUpdateTimeStamp = Calendar.getInstance().time
    }

    /**
     * Generate a random timestamp in the last 365 days .
     *
     * @return A random timestamp in the last year .
     */
    private fun randomDate(): Long {
        val random = Random()
        val now = System.currentTimeMillis()
        val year = random.nextDouble() * 1000 * 60 * 60 * 24 * 365
        return (now - year).toLong()
    }
}

open class RidesDBHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun get(arg: A): T {
        val checkInstance = instance
        if (checkInstance != null)
            return checkInstance
        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null)
                checkInstanceAgain
            else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}

