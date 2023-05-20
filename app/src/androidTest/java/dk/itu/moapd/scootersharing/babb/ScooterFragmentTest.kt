package dk.itu.moapd.scootersharing.babb

import android.util.Log
import org.junit.Test
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dk.itu.moapd.scootersharing.babb.viewmodel.ScooterFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class ScooterFragmentTest {

    private val TAG = "ScooterFragmentTest"

    @Mock
    private lateinit var database: DatabaseReference

    @Mock
    private lateinit var auth: FirebaseAuth

    @Mock
    private lateinit var task: Task<DataSnapshot>



    private lateinit var fragment: ScooterFragment

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fragment = ScooterFragment()
        fragment.database = database
    }

    @Test
    fun testTryFindScooterWithNonNullId() {
        val id = "scooterId"
        val name = "ScooterName"
        val snapshot = mockDataSnapshot(name)

        `when`(database.child("scooters").child(id)).thenReturn(database)
        `when`(database.get()).thenReturn(task)
        `when`(task.addOnSuccessListener(any())).thenAnswer { invocation ->
            val onSuccessListener = invocation.getArgument(0) as (DataSnapshot) -> Unit
            onSuccessListener.invoke(snapshot)
            task
        }

        fragment.tryFindScooter(id)

        // Verify that the expected methods were called
        assertEquals(name, fragment.binding.activeScooterName.text)
        // Add more assertions as needed
    }

    @Test
    fun testTryFindScooterWithNullId() {
        val id: String? = null

        fragment.tryFindScooter(id)

        // Verify that the expected methods were called
        // Add assertions for the Log statements as needed
    }

    private fun mockDataSnapshot(name: String): DataSnapshot {
        val snapshot: DataSnapshot = mock(DataSnapshot::class.java)
        val data: MutableMap<String, Any> = HashMap()
        data["name"] = name
        `when`(snapshot.value).thenReturn(data)
        return snapshot
    }

    // Helper method to mock an object
    private fun <T> mock(clazz: Class<T>): T {
        return org.mockito.Mockito.mock(clazz)
    }

}
