package dk.itu.moapd.scootersharing.babb.model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class ScooterLocation (
    val name: String,
    val latLng: LatLng,
    val address: LatLng,
    val rating: Float
        ) : Serializable {
    override fun toString(): String {
        return name
    }
}