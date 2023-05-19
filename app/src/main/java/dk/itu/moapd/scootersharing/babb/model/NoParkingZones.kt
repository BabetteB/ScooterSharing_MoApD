package dk.itu.moapd.scootersharing.babb.model

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil

class NoParkingZones {

    companion object{
        var appliedNoParkingZones: MutableList<List<LatLng>> = mutableListOf()

        private fun zoneCoordsList() : List<List<LatLng>> {
            val cphLakesCoordinates = listOf(
                LatLng(55.698012, 12.578683),
                LatLng(55.696150, 12.580185),
                LatLng(55.694215, 12.576408),
                LatLng(55.692668, 12.575216),
                LatLng(55.686670, 12.566151),
                LatLng(55.681378, 12.561136),
                LatLng(55.677923, 12.559989),
                LatLng(55.674247, 12.559022),
                LatLng(55.673378, 12.555654),
                LatLng(55.677863, 12.556048),
                LatLng(55.682509, 12.557517),
                LatLng(55.687498, 12.562533),
                LatLng(55.694041, 12.571812),
                LatLng(55.695818, 12.573747),
                LatLng(55.697998, 12.578584)
            )

            val iTULakesCoordinates = listOf(
                LatLng(55.663539, 12.590807),
                LatLng(55.663454, 12.592201),
                LatLng(55.656506, 12.589841),
                LatLng(55.656627, 12.588639),
                LatLng(55.663503, 12.590678)
            )

            val southHarbourCoordinates = listOf(
                LatLng(55.655628, 12.549322),
                LatLng(55.652293, 12.545321),
                LatLng(55.651062, 12.540502),
                LatLng(55.645059, 12.543866),
                LatLng(55.643160, 12.539865),
                LatLng(55.639927, 12.541047),
                LatLng(55.637976, 12.548959),
                LatLng(55.641364, 12.554233),
                LatLng(55.655423, 12.564599),
                LatLng(55.657013, 12.552323)
            )

            return listOf(cphLakesCoordinates , iTULakesCoordinates,southHarbourCoordinates)
        }


        private fun addShadedArea(googleMap: GoogleMap, polygonCoords : List<LatLng>) {
            // Define the coordinates of the shaded area polygon
            appliedNoParkingZones.add(polygonCoords)

            // Create a polygon options object and set its properties
            val polygonOptions = PolygonOptions()
                .addAll(polygonCoords)
                .fillColor(Color.argb(100, 255, 0, 0))  // Set the fill color (semi-transparent red)
                .strokeColor(Color.RED)                  // Set the stroke color (red)
                .strokeWidth(2f)                          // Set the stroke width

            // Add the polygon to the Google Map
            val polygon: Polygon = googleMap.addPolygon(polygonOptions)

            // Set other optional properties for the polygon
            polygon.isClickable = false



        }

        fun addDefaultNoParkingZones(googleMap: GoogleMap) {
            for (i in zoneCoordsList()) {
                addShadedArea(googleMap, i)
            }
        }

        fun isLatLngWithinPolygon(latLng: LatLng): Boolean {
            var inZone = false
            for (i in appliedNoParkingZones) {
                val b = PolyUtil.containsLocation(latLng, i, true)
                if (b) {
                    inZone = true
                }
            }
            return inZone
        }
    }


}