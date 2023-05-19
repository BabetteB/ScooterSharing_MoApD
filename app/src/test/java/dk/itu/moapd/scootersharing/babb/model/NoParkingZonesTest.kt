import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import dk.itu.moapd.scootersharing.babb.model.NoParkingZones
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class NoParkingZonesTest {

    @Mock
    private lateinit var googleMap: GoogleMap

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testIsLatLngWithinPolygon_InsidePolygon() {
        val latLng = LatLng(55.695329, 12.575534)

        // Mock the appliedNoParkingZones list
        val appliedNoParkingZones = mutableListOf(
            listOf(
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
        )
        NoParkingZones.appliedNoParkingZones = appliedNoParkingZones

        // Call the method under test
        val result = NoParkingZones.isLatLngWithinPolygon(latLng)

        // Verify that the result is true
        assertTrue(result)
    }

    @Test
    fun testIsLatLngWithinPolygon_OutsidePolygon() {
        val latLng = LatLng(55.670, 12.570)

        // Mock the appliedNoParkingZones list
        val appliedNoParkingZones = mutableListOf(
            listOf(
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
        )
        NoParkingZones.appliedNoParkingZones = appliedNoParkingZones

        // Call the method under test
        val result = NoParkingZones.isLatLngWithinPolygon(latLng)

        // Verify that the result is false
        assertFalse(result)
    }
}
