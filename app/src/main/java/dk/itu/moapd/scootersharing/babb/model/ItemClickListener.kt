package dk.itu.moapd.scootersharing.babb.model

interface ItemClickListener {

    fun onRideClicked(scooterId : String)

    fun onRideLongClicked(scooterId : String)

}