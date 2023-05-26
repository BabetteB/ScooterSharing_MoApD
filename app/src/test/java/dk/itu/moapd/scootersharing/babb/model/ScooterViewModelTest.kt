import dk.itu.moapd.scootersharing.babb.model.ScooterViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ScooterViewModelTest {

    private lateinit var viewModel: ScooterViewModel

    @Before
    fun setUp() {
        viewModel = ScooterViewModel()
    }

    @Test
    fun testSetActiveScooterId() {
        val scooterId = "123"
        viewModel.activeScooterId = scooterId
        assertEquals(scooterId, viewModel.activeScooterId)
    }

    @Test
    fun testSetActiveScooterId_Null() {
        viewModel.activeScooterId = null
        assertEquals(null, viewModel.activeScooterId)
    }
}
