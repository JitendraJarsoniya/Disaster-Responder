package com.example.byhm

import com.example.byhm.data.model.IncidentCategory
import com.example.byhm.data.model.IncidentReport
import com.example.byhm.data.model.IncidentStatus
import com.example.byhm.domain.PriorityLevel
import com.example.byhm.domain.RescuePriorityCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RescuePriorityCalculatorTest {

    @Test
    fun testCriticalFireWithTrappedVictims() {
        val report = IncidentReport(
            id = "inc_test_1",
            reporterId = "u1",
            reporterName = "Test User",
            category = IncidentCategory.FIRE,
            description = "Transformer explosion fire near market. 5 people trapped inside shop with injuries.",
            locationName = "Lajpat Nagar",
            latitude = 28.5,
            longitude = 77.2,
            timestamp = "10:00 IST",
            status = IncidentStatus.SUBMITTED,
            isVerified = false
        )

        val evaluation = RescuePriorityCalculator.calculatePriority(report)

        // Fire (30) + Trapped (25) + Injured (25) = 80 -> CRITICAL
        assertEquals(PriorityLevel.CRITICAL, evaluation.level)
        assertTrue(evaluation.score >= 60)
        assertTrue(evaluation.reasons.any { it.contains("thermal") })
        assertTrue(evaluation.reasons.any { it.contains("trapped") })
    }

    @Test
    fun testLowPriorityOtherEmergency() {
        val report = IncidentReport(
            id = "inc_test_2",
            reporterId = "u2",
            reporterName = "Test User 2",
            category = IncidentCategory.OTHER,
            description = "Tree branch fell in alleyway. Road partially clear.",
            locationName = "Sector 5",
            latitude = 28.6,
            longitude = 77.3,
            timestamp = "11:00 IST",
            status = IncidentStatus.SUBMITTED,
            isVerified = false
        )

        val evaluation = RescuePriorityCalculator.calculatePriority(report)

        // Other (10) -> LOW
        assertEquals(PriorityLevel.LOW, evaluation.level)
        assertTrue(evaluation.score < 20)
    }
}
