package com.example.byhm

import android.content.ContextWrapper
import android.content.res.AssetManager
import com.example.byhm.data.repository.EmergencyGuideRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTestContext : ContextWrapper(null) {
    override fun getAssets(): AssetManager {
        throw RuntimeException("Unit test environment - fallback to bundled offline assets")
    }
}

class EmergencyGuideRepositoryTest {

    @Test
    fun testOfflineGuidesLoadingAndCategories() {
        val testContext = FakeTestContext()
        val repository = EmergencyGuideRepository(testContext)

        val guides = repository.loadEmergencyGuides()

        assertNotNull(guides)
        assertEquals(4, guides.size)

        val categories = guides.map { it.category }
        assertTrue("Must contain Flood Safety guide", categories.any { it.contains("Flood", ignoreCase = true) })
        assertTrue("Must contain Fire Safety guide", categories.any { it.contains("Fire", ignoreCase = true) })
        assertTrue("Must contain Earthquake Safety guide", categories.any { it.contains("Earthquake", ignoreCase = true) })
        assertTrue("Must contain Severe Weather guide", categories.any { it.contains("Weather", ignoreCase = true) || it.contains("Lightning", ignoreCase = true) })

        for (guide in guides) {
            assertFalse("Guide title must not be blank", guide.title.isBlank())
            assertTrue("Guide warning must contain emergency 112 guidance", guide.emergencyWarning.contains("112"))
            assertFalse("Immediate actions must not be empty", guide.immediateActions.isEmpty())
            assertFalse("Actions to avoid must not be empty", guide.actionsToAvoid.isEmpty())
            assertFalse("Evacuation & shelter guidance must not be empty", guide.evacuationAndShelter.isEmpty())
            assertFalse("After-event precautions must not be empty", guide.afterEventPrecautions.isEmpty())
        }
    }
}
