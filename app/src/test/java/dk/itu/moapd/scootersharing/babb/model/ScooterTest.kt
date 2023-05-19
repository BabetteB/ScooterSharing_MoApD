import dk.itu.moapd.scootersharing.babb.model.Scooter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ScooterTest {

    private lateinit var scooter: Scooter

    @Before
    fun setUp() {
        scooter = Scooter()
    }

    @Test
    fun testSetId() {
        val id = "123"
        scooter.id = id
        assertEquals(id, scooter.id)
    }

    @Test
    fun testSetName() {
        val name = "Scooter X"
        scooter.name = name
        assertEquals(name, scooter.name)
    }

    @Test
    fun testSetLocationLat() {
        val lat = 51.5074
        scooter.locationLat = lat
        assertEquals(lat, scooter.locationLat!!, 0.0)
    }

    @Test
    fun testSetLocationLng() {
        val lng = -0.1278
        scooter.locationLng = lng
        assertEquals(lng, scooter.locationLng!!, 0.0)
    }

    @Test
    fun testSetReserved() {
        val reserved = true
        scooter.reserved = reserved
        assertEquals(reserved, scooter.reserved)
    }


}
